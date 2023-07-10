use std::fmt::{Debug, Display, Formatter};
use std::str::FromStr;

use crate::error::RolError;

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

impl Debug for Identifier {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        Display::fmt(self, f)
    }
}

impl FromStr for Identifier {
    type Err = RolError;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let mut parts = s.split(PACKAGE_SEP).collect::<Vec<_>>();
        let name = parts.pop().ok_or(RolError::Identifier(s.to_string()))?;
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