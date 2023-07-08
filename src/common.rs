use std::fmt::{Display, Formatter};

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