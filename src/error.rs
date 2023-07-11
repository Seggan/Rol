use std::fmt::Debug;
use std::path::Path;
use std::process::exit;

use thiserror::Error;

use crate::common::Position;

#[derive(Debug, Error)]
pub enum RolError {
    #[error("Failed to parse this identifier: {0}")]
    Identifier(String),
    #[error("Variable declarations cannot have a package specifier")]
    IllegalPackage,
}

impl RolError {
    pub fn report(&self) -> ! {
        eprintln!("Error: {}", self);
        exit(1)
    }
}

#[derive(Error, Debug)]
pub enum SyntaxError {
    #[error("Invalid number '{0}'")]
    InvalidNumber(String, Position),
    #[error("Invalid Unicode escape sequence '{0}'")]
    InvalidUnicodeEscape(String, Position),
    #[error("Unexpected character")]
    UnexpectedChar(Position),
    #[error("Unexpected end of file")]
    UnexpectedEof,
}

impl SyntaxError {
    pub fn report(&self, file: &Path, content: &str) -> ! {
        let pos = self.position();
        eprintln!("Syntax error: {}", self);
        if let Some(pos) = pos {
            eprintln!("  --> {}:{}:{}", file.to_string_lossy(), pos.line, pos.column);
            eprintln!("   | {}", content.lines().nth(pos.line - 1).unwrap());
            eprintln!("   | {:>width$}", "^", width = pos.column);
        } else {
            eprintln!("  --> {}", file.to_string_lossy());
        }
        exit(1)
    }

    fn position(&self) -> Option<Position> {
        match self {
            Self::InvalidUnicodeEscape(_, pos) => Some(*pos),
            Self::UnexpectedChar(pos) => Some(*pos),
            Self::UnexpectedEof => None,
        }
    }
}