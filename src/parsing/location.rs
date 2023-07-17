use std::cmp::{max, min};
use std::fmt::{Display, Formatter};
use std::ops::Add;
use std::rc::Rc;

use crate::parsing::lexer::Token;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Ord)]
pub struct Position {
    pub line: usize,
    pub column: usize,
}

impl Position {
    pub const NONE: Position = Position { line: 0, column: 0 };

    pub fn to_span(&self, text: &str) -> Span {
        Span::from_pos(*self, text)
    }

    pub fn location_in(&self, text: &str) -> usize {
        let line_lens: usize = text.lines()
            .take(self.line - 1)
            .map(str::len)
            .map(|c| c + 1)
            .sum();
        line_lens + self.column - 1
    }
}

impl Add<usize> for Position {
    type Output = Self;

    fn add(self, rhs: usize) -> Self::Output {
        Self {
            line: self.line,
            column: self.column + rhs,
        }
    }
}

impl Add<Position> for Position {
    type Output = Self;

    fn add(self, rhs: Position) -> Self::Output {
        Self {
            line: self.line + rhs.line,
            column: self.column + rhs.column,
        }
    }
}

impl PartialOrd for Position {
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        if self.line == other.line {
            self.column.partial_cmp(&other.column)
        } else {
            self.line.partial_cmp(&other.line)
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Span {
    pub start: Position,
    pub end: Position,
}

impl Display for Span {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        write!(f, "{}:{}", self.start.line, self.start.column)
    }
}

impl Span {
    pub const NONE: Span = Span {
        start: Position::NONE,
        end: Position::NONE,
    };

    pub fn new(start: Position, end: Position) -> Self {
        Self { start, end }
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
            column: new_column - 1,
        };
        Self {
            start: position,
            end,
        }
    }

    /// Places ASCII art matching the regex \^(-*\^)? under the span. If the span is multiline, returns None.
    pub fn caret(&self) -> Option<String> {
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

    pub fn text<'a>(&self, text: &'a str) -> &'a str {
        &text[self.start.location_in(text)..=self.end.location_in(text)]
    }

    pub fn merge(&self, other: &Self) -> Self {
        Self {
            start: min(self.start, other.start),
            end: max(self.end, other.end),
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct TokenSpan {
    pub start: usize,
    pub end: usize,
    tokens: Rc<Vec<Token>>
}

impl TokenSpan {

    pub fn new(start: usize, end: usize, tokens: Rc<Vec<Token>>) -> Self {
        Self { start, end, tokens }
    }

    pub fn none() -> Self {
        Self { start: 0, end: 0, tokens: Rc::new(Vec::new()) }
    }

    pub fn tokens(&self) -> &[Token] {
        &self.tokens[self.start..=self.end]
    }

    pub fn merge(&self, other: &Self) -> Self {
        Self {
            start: min(self.start, other.start),
            end: max(self.end, other.end),
            tokens: self.tokens.clone()
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_pos() {
        let pos = Position { line: 1, column: 1 };
        let span = pos.to_span("hello");
        assert_eq!(span.start, pos);
        assert_eq!(span.end, Position { line: 1, column: 5 });

        assert_eq!(pos.location_in("hello"), 0);
        assert_eq!(pos.location_in("hello\nworld"), 0);

        let pos = Position { line: 2, column: 1 };
        assert_eq!(pos.location_in("hello\nworld"), 6);

        let pos = Position { line: 2, column: 3 };
        assert_eq!(pos.location_in("hello\nworld"), 8);
    }

    #[test]
    fn test_span() {
        let span = Span::new(
            Position { line: 1, column: 1 },
            Position { line: 1, column: 5 },
        );

        assert_eq!(span.text("hello"), "hello");
        assert_eq!(span.caret(), Some("^---^".into()));

        let span = Span::new(
            Position { line: 1, column: 1 },
            Position { line: 1, column: 1 },
        );

        assert_eq!(span.text("hello"), "h");
        assert_eq!(span.caret(), Some("^".into()));

        let span = Span::new(
            Position { line: 1, column: 1 },
            Position { line: 1, column: 2 },
        );

        assert_eq!(span.text("hello"), "he");
        assert_eq!(span.caret(), Some("^^".into()));

        let span = Span::new(
            Position { line: 1, column: 3 },
            Position { line: 1, column: 5 },
        );

        assert_eq!(span.text("hello"), "llo");
        assert_eq!(span.caret(), Some("  ^-^".into()));

        let span = Span::new(
            Position { line: 1, column: 1 },
            Position { line: 2, column: 5 },
        );

        assert_eq!(span.text("hello\nworld"), "hello\nworld");
        assert_eq!(span.caret(), None);

        let span = Span::new(
            Position { line: 1, column: 1 },
            Position { line: 2, column: 1 },
        );

        assert_eq!(span.text("hello\nworld"), "hello\nw");
        assert_eq!(span.caret(), None);
    }
}
