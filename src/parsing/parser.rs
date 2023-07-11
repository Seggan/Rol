use enumset::{enum_set, EnumSet};

use crate::{error::SyntaxError, try_consume, parsing::ast::Modifier};

use super::{lexer::{Token, TokenType::*, RolKeyword::*, SingleChar::*}, ast::AstNode};

struct TokenStream {
    tokens: Vec<Token>,
    pos: usize,
    restore_stack: Vec<usize>
}

impl TokenStream {
    pub fn new(tokens: Vec<Token>) -> TokenStream {
        TokenStream { tokens, pos: 0, restore_stack: Vec::new() }
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

    pub fn save_pos(&mut self) {
        self.restore_stack.push(self.pos);
    }

    pub fn restore_pos(&mut self) {
        if let Some(pos) = self.restore_stack.pop() {
            self.pos = pos;
        }
    }

    pub fn drop_pos(&mut self) {
        self.restore_stack.pop();
    }
}

pub fn parse(tokens: Vec<Token>) -> Result<AstNode<()>, SyntaxError> {
    let mut tokens = TokenStream::new(tokens);
    let mut statements = Vec::new();
    while tokens.peek(1).is_some() {
        let stmt = parse_statement(&mut tokens)?;
        if let AstNode::Statements(stmts, _) = stmt {
            statements.extend(stmts);
        } else {
            statements.push(stmt);
        }
    }
    Ok(AstNode::Statements(statements, ()))
}

fn parse_statement(tokens: &mut TokenStream) -> Result<AstNode<()>, SyntaxError> {
    let stmt = parse_var_decl(tokens)?;
    if let Some(token) = tokens.next() {
        if token.token_type == Newline {
            Ok(stmt)
        } else {
            Err(SyntaxError::ExpectedToken("a newline".to_string(), token.position))
        }
    } else {
        Ok(stmt)
    }
}

fn parse_var_decl(tokens: &mut TokenStream) -> Result<AstNode<()>, SyntaxError> {
    let keyword = if let Keyword(keyword) = try_consume!(
        tokens, 
        Keyword(Val) | Keyword(Var), 
        "'val' or 'var'"
    ).token_type {
        keyword
    } else {
        unreachable!()
    };
    let modifiers = if keyword == Val {enum_set!{Modifier::Final}} else {enum_set!{}};
    let name = try_consume!(tokens, Identifier, "an identifier").text.clone();
    try_consume!(tokens, SingleChar(Equals), "'='");
    Ok(AstNode::VarDecl(modifiers, name.parse()?, ()))
}

fn parse_maybe(
    tokens: &mut TokenStream, 
    func: impl FnOnce(&mut TokenStream) -> Result<AstNode<()>, SyntaxError>
) -> Option<AstNode<()>> {
    tokens.save_pos();
    if let Ok(result) = func(tokens) {
        tokens.drop_pos();
        Some(result)
    } else {
        tokens.restore_pos();
        None
    }
}