use std::fmt::{Display, Formatter};
use std::str::FromStr;

use crate::error::Error;

pub struct Identifier {
    pub package: Option<String>,
    pub name: String
}

const PACKAGE_SEP: &str = "::";

impl Display for Identifier {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        if let Some(package) = &self.package {
            write!(f, "{}{}{}", package, PACKAGE_SEP, self.name)
        } else {
            write!(f, "{}", self.name)
        }
    }
}

impl FromStr for Identifier {
    type Err = Error;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let mut parts = s.split(PACKAGE_SEP).collect::<Vec<_>>();
        let name = parts.pop().ok_or(Error::IdentifierParseError(s.to_string()))?;
        let package = if parts.is_empty() {
            None
        } else {
            Some(parts.join(PACKAGE_SEP))
        };
        Ok(Identifier {
            package,
            name: name.to_string()
        })
    }
}