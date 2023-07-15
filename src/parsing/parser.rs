use enumset::{enum_set, EnumSet};

use crate::{consumes_end, match_next, try_consume};
use crate::error::RolError;
use crate::error::SyntaxError;
use crate::parsing::ast::{Literal, Modifier, PostfixOp, PrefixOp};
use crate::parsing::location::Span;

use super::ast::{AstNode, BinOp, Expr};
use super::lexer::{RolKeyword::*, SingleChar::*, Token, TokenType::*};

struct TokenStream {
    tokens: Vec<Token>,
    pos: usize
}

impl TokenStream {
    pub fn new(tokens: Vec<Token>) -> TokenStream {
        TokenStream { tokens, pos: 0 }
    }

    pub fn next(&mut self) -> Option<&Token> {
        let res = self.tokens.get(self.pos);
        self.pos += 1;
        res
    }

    pub fn previous(&mut self) -> Option<&Token> {
        let res = self.tokens.get(self.pos);
        self.pos -= 1;
        res
    }

    pub fn peek(&self, n: usize) -> Option<&Token> {
        self.tokens.get(self.pos + n - 1)
    }

    pub fn current(&self) -> Option<&Token> {
        self.peek(0)
    }
}

type NodeResult = Result<AstNode<Span>, RolError>;
type ExprResult = Result<Expr<Span>, RolError>;

pub fn parse(tokens: Vec<Token>) -> NodeResult {
    let mut tokens = TokenStream::new(tokens);
    let mut statements = Vec::new();
    let mut errors = Vec::new();
    while tokens.peek(1).is_some() {
        let stmt = match parse_statement(&mut tokens) {
            Ok(stmt) => stmt,
            Err(err) => {
                if matches!(syntax_errors(&err).last().unwrap(), SyntaxError::ExpectedNewline(_)) {
                    errors.push(err);
                    continue;
                } else {
                    while let Some(token) = tokens.next() {
                        if token.token_type == Newline {
                            break;
                        }
                    }
                }
                errors.push(err);
                continue;
            }
        };
        if let AstNode::Statements(stmts, _) = stmt {
            statements.extend(stmts);
        } else {
            statements.push(stmt);
        }
    }
    consumes_end!(errors);
    Ok(AstNode::Statements(statements, todo!()))
}

fn parse_statement(tokens: &mut TokenStream) -> NodeResult {
    let stmt = parse_var_decl(tokens)?;
    if let Some(token) = tokens.next() {
        if token.token_type != Newline {
            let pos = token.span.clone();
            return Err(SyntaxError::ExpectedNewline(pos).into())
        }
    }
    Ok(stmt)
}

fn parse_var_decl(tokens: &mut TokenStream) -> NodeResult {
    let mut errors = Vec::new();
    try_consume!(tokens, errors, Keyword(Val) | Keyword(Var), "'val' or 'var'");
    let keyword = if let Some(token) = tokens.current() {
        if let Keyword(keyword) = token.token_type {
            keyword
        } else {
            unreachable!()
        }
    } else {
        unreachable!()
    };
    let modifiers = if keyword == Val {enum_set!{Modifier::Final}} else {enum_set!{}};

    ignore_newlines(tokens);
    let name: super::common::Identifier = try_consume!(tokens, Identifier, "an identifier").text.clone().parse()?;
    ignore_newlines(tokens);
    try_consume!(tokens, errors, SingleChar(Equals), "'='");
    ignore_newlines(tokens);
    let expr = consumes_end!(errors, parse_expr(tokens));
    Ok(AstNode::Statements(vec![
        AstNode::VarDecl(modifiers, name.clone(), todo!()),
        AstNode::VarAssign(name, expr, todo!())
    ], todo!()))
}

fn parse_expr(tokens: &mut TokenStream) -> ExprResult {
    let mut left = parse_and(tokens)?;
    while match_next!(tokens, Or).is_some() {
        let right = parse_and(tokens)?;
        left = Expr::BinOp(left.into(), BinOp::Or, right.into(), todo!());
    }
    Ok(left)
}

fn parse_and(tokens: &mut TokenStream) -> ExprResult {
    let mut left = parse_equality(tokens)?;
    while match_next!(tokens, And).is_some() {
        let right = parse_equality(tokens)?;
        left = Expr::BinOp(left.into(), BinOp::And, right.into(), todo!());
    }
    Ok(left)
}

fn parse_equality(tokens: &mut TokenStream) -> ExprResult {
    let mut left = parse_comparison(tokens)?;
    while let Some(token) = match_next!(tokens, DoubleEquals | NotEquals) {
        let right = parse_comparison(tokens)?;
        left = Expr::BinOp(
            left.into(),
            match token.token_type {
                DoubleEquals => BinOp::Equals,
                NotEquals => BinOp::NotEquals,
                _ => unreachable!()
            },
            right.into(),
            todo!()
        );
    }
    Ok(left)
}

fn parse_comparison(tokens: &mut TokenStream) -> ExprResult {
    let mut left = parse_term(tokens)?;
    while let Some(token) = match_next!(tokens, SingleChar(LessThan) | LessThanEquals | SingleChar(GreaterThan) | GreaterThanEquals) {
        let right = parse_term(tokens)?;
        left = Expr::BinOp(
            left.into(),
            match token.token_type {
                SingleChar(LessThan) => BinOp::LessThan,
                LessThanEquals => BinOp::LessThanOrEqual,
                SingleChar(GreaterThan) => BinOp::GreaterThan,
                GreaterThanEquals => BinOp::GreaterThanOrEqual,
                _ => unreachable!()
            },
            right.into(),
            todo!()
        );
    }
    Ok(left)
}

fn parse_term(tokens: &mut TokenStream) -> ExprResult {
    let mut left = parse_factor(tokens)?;
    while let Some(token) = match_next!(tokens, SingleChar(Plus) | SingleChar(Minus)) {
        let right = parse_factor(tokens)?;
        left = Expr::BinOp(
            left.into(),
            match token.token_type {
                SingleChar(Plus) => BinOp::Plus,
                SingleChar(Minus) => BinOp::Minus,
                _ => unreachable!()
            },
            right.into(),
            todo!()
        );
    }
    Ok(left)
}

fn parse_factor(tokens: &mut TokenStream) -> ExprResult {
    let mut left = parse_unary(tokens)?;
    while let Some(token) = match_next!(tokens, SingleChar(Star) | SingleChar(Slash) | SingleChar(Percent)) {
        let right = parse_unary(tokens)?;
        left = Expr::BinOp(
            left.into(),
            match token.token_type {
                SingleChar(Star) => BinOp::Times,
                SingleChar(Slash) => BinOp::Divide,
                SingleChar(Percent) => BinOp::Modulo,
                _ => unreachable!()
            },
            right.into(),
            todo!()
        );
    }
    Ok(left)
}

fn parse_unary(tokens: &mut TokenStream) -> ExprResult {
    if let Some(token) = match_next!(tokens, SingleChar(Minus) | SingleChar(Not)) {
        let right = parse_unary(tokens)?;
        Ok(Expr::PrefixOp(
            match token.token_type {
                SingleChar(Minus) => PrefixOp::Negate,
                SingleChar(Not) => PrefixOp::Not,
                _ => unreachable!()
            },
            right.into(),
            todo!()
        ))
    } else {
        parse_power(tokens)
    }
}

fn parse_power(tokens: &mut TokenStream) -> ExprResult {
    let mut left = parse_postfix(tokens)?;
    if match_next!(tokens, DoubleStar).is_some() {
        let right = parse_power(tokens)?;
        left = Expr::BinOp(left.into(), BinOp::Power, right.into(), todo!());
    }
    Ok(left)
}

fn parse_postfix(tokens: &mut TokenStream) -> ExprResult {
    let mut errors = Vec::new();
    let mut left = parse_primary(tokens)?;
    let start_pos = left.extra_data().start;
    while match_next!(tokens, SingleChar(OpenParen)).is_some() {
        let mut args = Vec::new();
        if match_next!(tokens, SingleChar(CloseParen)).is_none() {
            loop {
                args.push(parse_expr(tokens)?);
                if match_next!(tokens, SingleChar(CloseParen)).is_some() {
                    break;
                }
                try_consume!(tokens, errors, SingleChar(Comma), "','");
            }
        }
        left = Expr::PostfixOp(
            left.into(),
            PostfixOp::FunctionCall(args),
            todo!()
        );
    }
    consumes_end!(errors);
    Ok(left)
}

fn parse_primary(tokens: &mut TokenStream) -> ExprResult {
    if let Some(token) = tokens.next() {
        let span = token.span.clone();
        match token.token_type {
            Identifier => Ok(Expr::VarAccess(token.text.parse()?, span)),
            Number => Ok(Expr::Literal(Literal::Float(token.text.clone()), span)),
            _ => Err(SyntaxError::UnexpectedToken(span).into())
        }
    } else {
        Err(SyntaxError::UnexpectedEof.into())
    }
}

fn ignore_newlines(tokens: &mut TokenStream) {
    while let Some(next) = tokens.peek(1) {
        if next.token_type == Newline {
            tokens.next();
        } else {
            break;
        }
    }
}

fn syntax_errors(err: &RolError) -> Vec<&SyntaxError> {
    match err {
        RolError::Syntax(err) => vec![err],
        RolError::Multiple(errs) => errs.iter().flat_map(syntax_errors).collect(),
        _ => Vec::new()
    }
}