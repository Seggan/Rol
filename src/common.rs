use std::fmt::{Debug, Display, Formatter};
use std::ops::Add;
use std::str::FromStr;

use crate::error::SyntaxError;

#[derive(Clone, PartialEq, Eq)]
pub struct Identifier {
    pub package: Option<String>,
    pub name: String
}

impl Display for Identifier {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        if let Some(package) = &self.package {
            write!(f, "{}{}{}", package, Self::PACKAGE_SEP, self.name)
        } else {
            write!(f, "{}", self.name)
        }
    }
}

impl Debug for Identifier {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        Display::fmt(self, f)
    }
}

impl FromStr for Identifier {
    type Err = SyntaxError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let mut parts = s.split(Self::PACKAGE_SEP).collect::<Vec<_>>();
        let name = parts.pop().ok_or(Self::Err::IdentifierParseError(s.to_string()))?;
        let package = if parts.is_empty() {
            None
        } else {
            Some(parts.join(Self::PACKAGE_SEP))
        };
        Ok(Identifier {
            package,
            name: name.to_string()
        })
    }
}

impl Identifier {
    pub const PACKAGE_SEP: &'static str = "::";
}



pub trait SourceLocation {
    fn caret(&self) -> Option<String>;
    fn line(&self) -> usize;
    fn column(&self) -> usize;
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct Position {
    pub line: usize,
    pub column: usize
}

impl Position {
    pub fn to_span(&self, text: &str) -> Span {
        Span::from_pos(*self, text)
    }
}

impl Display for Position {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}:{}", self.line, self.column)
    }
}

impl Add<usize> for Position {
    type Output = Self;

    fn add(self, rhs: usize) -> Self::Output {
        Self {
            line: self.line,
            column: self.column + rhs
        }
    }
}

impl Add<Position> for Position {
    type Output = Self;

    fn add(self, rhs: Position) -> Self::Output {
        Self {
            line: self.line + rhs.line,
            column: self.column + rhs.column
        }
    }
}

impl SourceLocation for Position {
    fn caret(&self) -> Option<String> {
        format!("{:>width$}", "^", width = self.column).into()
    }

    fn line(&self) -> usize {
        self.line
    }

    fn column(&self) -> usize {
        self.column
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Span {
    pub start: Position,
    pub end: Position,
    pub text: String
}

impl Display for Span {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}-{}: {}", self.start, self.end, self.text)
    }
}

impl Span {
    pub fn new(start: Position, end: Position, text: String) -> Self {
        Self { start, end, text }
    }

    pub fn from_pos(position: Position, text: &str) -> Self {
        let lines = text.lines().collect::<Vec<_>>();
        let line = lines.len();
        let column = lines.last().unwrap().len();
        let new_column = if line == 1 {
            position.column + column
        } else {
            column
        };
        let end = Position {
            line: position.line + line - 1,
            column: new_column - 1
        };
        Self {
            start: position,
            end,
            text: text.to_string()
        }
    }
}

impl SourceLocation for Span {
    fn caret(&self) -> Option<String> {
        if self.start.line == self.end.line {
            if self.start.column == self.end.column {
                format!("{:>width$}", "^", width = self.start.column).into()
            } else {
                format!(
                    "{:>start$}{:->width$}",
                    "^",
                    "^",
                    start = self.start.column,
                    width = self.end.column - self.start.column
                ).into()
            }
        } else {
            None
        }
    }

    fn line(&self) -> usize {
        self.start.line
    }

    fn column(&self) -> usize {
        self.start.column
    }
}