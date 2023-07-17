use std::fmt::{Debug, Display, Formatter};
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

pub enum Type {
    Int,
    Float,
    String,
    Bool,
    Void,
    Function(Vec<Type>, Box<Type>),
    Struct(Identifier),
    Enum(Identifier),
    Generic(Identifier),
    Unknown(Identifier)
}