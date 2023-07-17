use crate::error::RolError;
use crate::parsing::{lexer, parser};

pub mod parsing;
pub mod error;

pub fn process_file(contents: &str) -> Result<(), RolError> {
    let tokens = lexer::lex(&contents)?;
    let ast = parser::parse(tokens)?;
    println!("{}", ast);
    Ok(())
}