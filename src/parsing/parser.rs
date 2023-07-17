use std::rc::Rc;

use enumset::{enum_set, EnumSet};

use crate::{binop, consumes_end, match_next, try_consume};
use crate::error::RolError;
use crate::error::SyntaxError;
use crate::parsing::ast::{Literal, Modifier, PostfixOp, PrefixOp};
use crate::parsing::location::TokenSpan;

use super::ast::{AstNode, BinOp, Expr};
use super::lexer::{RolKeyword::*, SingleChar::*, Token, TokenType::*};

struct TokenStream {
    tokens: Rc<Vec<Token>>,
    pos: usize,
}

impl TokenStream {
    fn next(&mut self) -> Option<&Token> {
        let res = self.tokens.get(self.pos);
        self.pos += 1;
        res
    }

    fn previous(&mut self) -> Option<&Token> {
        self.pos -= 1;
        self.tokens.get(self.pos)
    }

    fn peek(&self, n: usize) -> Option<&Token> {
        self.tokens.get(self.pos + n - 1)
    }

    fn current(&self) -> Option<&Token> {
        self.peek(0)
    }

    fn current_pos(&self) -> usize {
        self.pos - 1
    }
}

type NodeResult = Result<AstNode<TokenSpan>, RolError>;
type ExprResult = Result<Expr<TokenSpan>, RolError>;

pub fn parse(tokens: Vec<Token>) -> NodeResult {
    let raw = Rc::new(tokens);
    let mut tokens = TokenStream {
        tokens: raw.clone(),
        pos: 0,
    };
    parse_statements(&mut tokens, raw)
}

fn parse_statements(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> NodeResult {
    let mut statements = Vec::new();
    let mut errors = Vec::new();
    while tokens.peek(1).is_some() {
        let stmt = match parse_statement(tokens, raw.clone()) {
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
    let span = if let Some(first) = statements.first() {
        first.extra_data().merge(statements.last().unwrap().extra_data())
    } else {
        TokenSpan::none()
    };
    Ok(AstNode::Statements(
        statements,
        span,
    ))
}

fn parse_statement(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> NodeResult {
    let stmt = parse_var_decl(tokens, raw)?;
    if let Some(token) = tokens.next() {
        if token.token_type != Newline {
            let pos = token.span.clone();
            return Err(SyntaxError::ExpectedNewline(pos).into());
        }
    }
    Ok(stmt)
}

fn parse_var_decl(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> NodeResult {
    let start = tokens.pos;
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
    let modifiers = if keyword == Val { enum_set! {Modifier::Final} } else { enum_set! {} };

    ignore_newlines(tokens);
    let first_start = tokens.pos;
    let name: super::common::Identifier = try_consume!(tokens, Identifier, "an identifier").text.clone().parse()?;
    ignore_newlines(tokens);
    let end = tokens.pos;
    try_consume!(tokens, errors, SingleChar(Equals), "'='");
    ignore_newlines(tokens);
    let expr = consumes_end!(errors, parse_expr(tokens, raw.clone()));
    let expr_span = expr.extra_data().clone();
    Ok(AstNode::Statements(vec![
        AstNode::VarDecl(modifiers, name.clone(), TokenSpan::new(start, end, raw.clone())),
        AstNode::VarAssign(name, expr, TokenSpan::new(first_start, expr_span.end, raw.clone())),
    ], TokenSpan::new(start, expr_span.end, raw)))
}

fn parse_expr(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    binop!(tokens, raw, parse_and, Or => BinOp::Or)
}

fn parse_and(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    binop!(tokens, raw, parse_equality, And => BinOp::And)
}

fn parse_equality(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    binop!(tokens, raw, parse_comparison, DoubleEquals => BinOp::Equals, NotEquals => BinOp::NotEquals)
}

fn parse_comparison(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    binop!(
        tokens,
        raw,
        parse_term,
        SingleChar(LessThan) => BinOp::LessThan,
        LessThanEquals => BinOp::LessThanOrEqual,
        SingleChar(GreaterThan) => BinOp::GreaterThan,
        GreaterThanEquals => BinOp::GreaterThanOrEqual
    )
}

fn parse_term(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    binop!(tokens, raw, parse_factor, SingleChar(Plus) => BinOp::Plus, SingleChar(Minus) => BinOp::Minus)
}

fn parse_factor(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    binop!(
        tokens,
        raw,
        parse_unary,
        SingleChar(Star) => BinOp::Times,
        SingleChar(Slash) => BinOp::Divide,
        SingleChar(Percent) => BinOp::Modulo
    )
}

fn parse_unary(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    let pos = tokens.pos;
    if let Some(token) = match_next!(tokens, SingleChar(Minus) | SingleChar(Not)) {
        let right = parse_unary(tokens, raw.clone())?;
        let right_end = right.extra_data().end;
        Ok(Expr::PrefixOp(
            match token.token_type {
                SingleChar(Minus) => PrefixOp::Negate,
                SingleChar(Not) => PrefixOp::Not,
                _ => unreachable!()
            },
            right.into(),
            TokenSpan::new(pos, right_end, raw.clone()),
        ))
    } else {
        parse_power(tokens, raw)
    }
}

fn parse_power(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    let mut left = parse_postfix(tokens, raw.clone())?;
    if match_next!(tokens, DoubleStar).is_some() {
        let right = parse_power(tokens, raw.clone())?;
        let right_end = right.extra_data().end;
        let left_start = left.extra_data().start;
        left = Expr::BinOp(
            left.into(),
            BinOp::Power,
            right.into(),
            TokenSpan::new(left_start, right_end, raw.clone()),
        );
    }
    Ok(left)
}

fn parse_postfix(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    let mut errors = Vec::new();
    let mut left = parse_primary(tokens, raw.clone())?;
    while match_next!(tokens, SingleChar(OpenParen)).is_some() {
        let mut args = Vec::new();
        if match_next!(tokens, SingleChar(CloseParen)).is_none() {
            loop {
                args.push(parse_expr(tokens, raw.clone())?);
                if match_next!(tokens, SingleChar(CloseParen)).is_some() {
                    break;
                }
                try_consume!(tokens, errors, SingleChar(Comma), "','");
            }
        }
        let left_start = left.extra_data().start;
        left = Expr::PostfixOp(
            left.into(),
            PostfixOp::FunctionCall(args),
            TokenSpan::new(left_start, tokens.current_pos(), raw.clone()),
        );
    }
    consumes_end!(errors);
    Ok(left)
}

fn parse_primary(tokens: &mut TokenStream, raw: Rc<Vec<Token>>) -> ExprResult {
    if let Some(token) = tokens.next() {
        let tok_span = token.span.clone();
        match token.token_type {
            Identifier => Ok(Expr::VarAccess(
                token.text.parse()?,
                TokenSpan::new(tokens.current_pos(), tokens.current_pos(), raw.clone())
            )),
            Number => Ok(Expr::Literal(
                Literal::Float(token.text.clone()),
                TokenSpan::new(tokens.current_pos(), tokens.current_pos(), raw.clone())
            )),
            _ => Err(SyntaxError::UnexpectedToken(tok_span).into())
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

#[allow(unreachable_patterns)]
fn syntax_errors(err: &RolError) -> Vec<&SyntaxError> {
    match err {
        RolError::Syntax(err) => vec![err],
        RolError::Multiple(errs) => errs.iter().flat_map(syntax_errors).collect(),
        _ => Vec::new()
    }
}