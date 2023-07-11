use crate::{error::SyntaxError, try_consume, parsing::lexer::{TokenType, Keyword}};

use super::{lexer::Token, ast::AstNode};

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
        self.tokens.get(self.pos + n)
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
    try_consume!(tokens, TokenType::Keyword(Keyword::If), "'if'");
    todo!()
}

fn parse_maybe(
    tokens: &mut TokenStream, 
    func: fn(&mut TokenStream) -> Result<AstNode<()>, SyntaxError>
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