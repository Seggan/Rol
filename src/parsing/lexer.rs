use std::fmt::Debug;
use std::iter::Peekable;
use std::str::Chars;

use unicode_ident::{is_xid_continue, is_xid_start};
use unicode_normalization::UnicodeNormalization;

use crate::error::SyntaxError;
use crate::parsing::location::{Position, Span};

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Token {
    pub token_type: TokenType,
    pub span: Span,
    pub text: String,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum TokenType {
    And,
    DoubleColon,
    DoubleEquals,
    GreaterThanEquals,
    Identifier,
    Keyword(RolKeyword),
    LessThanEquals,
    Newline,
    NotEquals,
    Number,
    Or,
    SingleChar(SingleChar),
    DoubleStar,
    String(String),
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum RolKeyword {
    Else,
    If,
    Val,
    Var,
}

impl RolKeyword {
    pub fn from_str(s: &str) -> Option<RolKeyword> {
        match s {
            "else" => Some(RolKeyword::Else),
            "if" => Some(RolKeyword::If),
            "val" => Some(RolKeyword::Val),
            "var" => Some(RolKeyword::Var),
            _ => None
        }
    }
}

#[derive(Debug, Clone, Copy, Eq, PartialEq)]
pub enum SingleChar {
    Comma,
    Equals,
    GreaterThan,
    LessThan,
    Minus,
    Not,
    OpenParen,
    CloseParen,
    Percent,
    Plus,
    Slash,
    Star,
}

impl SingleChar {
    fn from_char(c: char) -> Option<SingleChar> {
        match c {
            ',' => Some(Self::Comma),
            '>' => Some(Self::GreaterThan),
            '<' => Some(Self::LessThan),
            '-' => Some(Self::Minus),
            '!' => Some(Self::Not),
            '(' => Some(Self::OpenParen),
            ')' => Some(Self::CloseParen),
            '+' => Some(Self::Plus),
            '%' => Some(Self::Percent),
            '/' => Some(Self::Slash),
            _ => None
        }
    }
}

pub fn lex(code: &str) -> Result<Vec<Token>, SyntaxError> {
    let mut tokens: Vec<Token> = Vec::new();
    let mut chars = code.chars().peekable();
    let mut position = Position { line: 1, column: 1 };
    while let Some(c) = chars.next() {
        let pos = position;
        let token = if is_xid_start(c) {
            let mut ident = String::new();
            ident.push(c);
            while let Some(&c) = chars.peek() {
                if is_xid_continue(c) {
                    ident.push(c);
                    chars.next();
                    position.column += 1;
                } else {
                    break;
                }
            }
            if let Some(keyword) = RolKeyword::from_str(&ident) {
                (TokenType::Keyword(keyword), ident)
            } else {
                (TokenType::Identifier, ident.nfc().collect())
            }
        } else if c == ':' {
            if let Some(&':') = chars.peek() {
                chars.next();
                position.column += 1;
                (TokenType::DoubleColon, "::".to_string())
            } else {
                return Err(SyntaxError::UnexpectedChar(position.to_span("")));
            }
        } else if c == '|' {
            if let Some(&'|') = chars.peek() {
                chars.next();
                position.column += 1;
                (TokenType::Or, "||".to_string())
            } else {
                return Err(SyntaxError::UnexpectedChar(position.to_span("")));
            }
        } else if c == '&' {
            if let Some(&'&') = chars.peek() {
                chars.next();
                position.column += 1;
                (TokenType::And, "&&".to_string())
            } else {
                return Err(SyntaxError::UnexpectedChar(position.to_span("")));
            }
        } else if c == '\r' || c == '\n' {
            position.line += 1;
            if let Some(last) = tokens.last() {
                if last.token_type == TokenType::Newline {
                    continue;
                }
            }
            if let Some(&'\n') = chars.peek() {
                if c == '\r' {
                    chars.next();
                }
            }
            position.column = 0;
            (TokenType::Newline, "\n".to_string())
        } else if c == '"' {
            let s = lex_string(&mut chars, &mut position)?;
            (TokenType::String(s.clone()), format!("\"{s}\""))
        } else if ('0'..='9').contains(&c) {
            (TokenType::Number, lex_number(&mut chars, &mut position, c)?)
        } else if c == '=' {
            if let Some(&'=') = chars.peek() {
                chars.next();
                position.column += 1;
                (TokenType::DoubleEquals, "==".to_string())
            } else {
                (TokenType::SingleChar(SingleChar::Equals), "=".to_string())
            }
        } else if c == '<' {
            if let Some(&'=') = chars.peek() {
                chars.next();
                position.column += 1;
                (TokenType::LessThanEquals, "<=".to_string())
            } else {
                (TokenType::SingleChar(SingleChar::LessThan), "<".to_string())
            }
        } else if c == '>' {
            if let Some(&'=') = chars.peek() {
                chars.next();
                position.column += 1;
                (TokenType::GreaterThanEquals, ">=".to_string())
            } else {
                (TokenType::SingleChar(SingleChar::GreaterThan), ">".to_string())
            }
        } else if c == '*' {
            if let Some(&'*') = chars.peek() {
                chars.next();
                position.column += 1;
                (TokenType::DoubleStar, "**".to_string())
            } else {
                (TokenType::SingleChar(SingleChar::Star), "*".to_string())
            }
        } else if c == '!' {
            if let Some(&'=') = chars.peek() {
                chars.next();
                position.column += 1;
                (TokenType::NotEquals, "!=".to_string())
            } else {
                (TokenType::SingleChar(SingleChar::Not), "!".to_string())
            }
        } else if let Some(single) = SingleChar::from_char(c) {
            (TokenType::SingleChar(single), c.to_string())
        } else if c.is_whitespace() {
            position.column += 1;
            continue;
        } else {
            return Err(SyntaxError::UnexpectedChar(position.to_span("")));
        };
        tokens.push(Token { token_type: token.0, span: pos.to_span(&token.1), text: token.1 });
        position.column += 1;
    }
    Ok(tokens)
}


fn lex_string(chars: &mut Peekable<Chars>, position: &mut Position) -> Result<String, SyntaxError> {
    let mut string = String::new();
    while let Some(&c) = chars.peek() {
        match c {
            '"' => {
                chars.next();
                position.column += 1;
                break;
            }
            '\\' => {
                chars.next();
                position.column += 1;
                match chars.next() {
                    Some('u') => {
                        // We let Lua handle the escape sequences, except for Unicode escapes
                        // which we handle here.
                        let mut unicode = String::new();
                        for _ in 0..4 {
                            if let Some(c) = chars.next() {
                                unicode.push(c);
                                position.column += 1;
                            } else {
                                return Err(SyntaxError::UnexpectedEof);
                            }
                        }
                        if let Ok(codepoint) = u32::from_str_radix(&unicode, 16) {
                            if let Some(c) = char::from_u32(codepoint) {
                                string.push(c);
                            } else {
                                let span = position.to_span(&unicode);
                                return Err(SyntaxError::InvalidUnicodeEscape(unicode, span));
                            }
                        } else {
                            let span = position.to_span(&unicode);
                            return Err(SyntaxError::InvalidUnicodeEscape(unicode, span));
                        }
                    }
                    Some(c) => {
                        string.push('\\');
                        string.push(c);
                        position.column += 1;
                    }
                    None => {}
                }
            }
            _ => {
                string.push(c);
                chars.next();
                position.column += 1;
                if c == '\n' {
                    position.line += 1;
                    position.column = 0;
                }
            }
        }
    }
    Ok(string)
}

fn lex_number(chars: &mut Peekable<Chars>, position: &mut Position, first: char) -> Result<String, SyntaxError> {
    let pos = *position;
    let mut number = String::from(first);
    while let Some(&c) = chars.peek() {
        if c.is_digit(10) || c == '.' {
            number.push(c);
            chars.next();
            position.column += 1;
        } else {
            break;
        }
    }
    if let Ok(_) = number.parse::<f64>() {
        Ok(number)
    } else {
        let span = pos.to_span(&number);
        Err(SyntaxError::InvalidNumber(number, span))
    }
}