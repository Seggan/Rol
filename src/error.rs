use std::fmt::Debug;
use std::path::Path;
use std::process::exit;

use thiserror::Error;

use crate::parsing::location::Span;

#[derive(Error, Debug)]
pub enum RolError {
    #[error("Syntax error")]
    Syntax(SyntaxError),
    #[error("Multiple errors")]
    Multiple(Vec<RolError>),
}

impl RolError {
    pub fn report(&self, file: &Path, content: &str) {
        match self {
            Self::Syntax(err) => err.report(file, content),
            Self::Multiple(errors) => {
                for error in errors {
                    error.report(file, content);
                }
            }
        }
    }

    pub fn report_and_exit(&self, file: &Path, content: &str) -> ! {
        self.report(file, content);
        exit(1);
    }
}

impl From<SyntaxError> for RolError {
    fn from(error: SyntaxError) -> Self {
        Self::Syntax(error)
    }
}

#[derive(Error, Debug)]
pub enum SyntaxError {
    #[error("Expected a new line")]
    ExpectedNewline(Span),
    #[error("Expected {0}")]
    ExpectedToken(String, Span),
    #[error("Failed to parse the identifier '{0}' (this shouldn't happen)")]
    IdentifierParseError(String),
    #[error("Invalid number '{0}'")]
    InvalidNumber(String, Span),
    #[error("Invalid Unicode escape sequence '{0}'")]
    InvalidUnicodeEscape(String, Span),
    #[error("Unexpected character")]
    UnexpectedChar(Span),
    #[error("Unexpected end of file")]
    UnexpectedEof,
    #[error("Unexpected token")]
    UnexpectedToken(Span),
}

impl SyntaxError {
    fn location(&self) -> Option<&Span> {
        match self {
            Self::ExpectedNewline(pos) => Some(pos),
            Self::ExpectedToken(_, pos) => Some(pos),
            Self::IdentifierParseError(_) => None,
            Self::InvalidNumber(_, pos) => Some(pos),
            Self::InvalidUnicodeEscape(_, pos) => Some(pos),
            Self::UnexpectedChar(pos) => Some(pos),
            Self::UnexpectedEof => None,
            Self::UnexpectedToken(pos) => Some(pos),
        }
    }

    fn report(&self, file: &Path, content: &str) {
        let pos = self.location();
        eprintln!("Syntax error: {}", self);
        if let Some(pos) = pos {
            eprintln!("  --> {}:{}:{}", file.to_string_lossy(), pos.start.line, pos.start.column);
            if let Some(caret) = pos.caret() {
                eprintln!("   | {}", content.lines().nth(pos.start.line - 1).unwrap());
                eprintln!("   | {}", caret);
            }
        } else {
            eprintln!("  --> {}", file.to_string_lossy());
        }
    }
}