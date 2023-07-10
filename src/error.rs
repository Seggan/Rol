use std::process::exit;

use thiserror::Error;

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