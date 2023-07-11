use std::env;
use std::fs::read_to_string;
use std::path::Path;

use crate::parsing::lexer;

mod parsing;
mod common;
mod error;

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if let Some(file) = args.get(1) {
        let file = Path::new(file);
        let text = read_to_string(file).unwrap();
        let tokens = lexer::lex(&text);
        match tokens {
            Ok(tokens) => println!("{:?}", tokens.into_iter().map(|t| t.token_type).collect::<Vec<_>>()),
            Err(error) => error.report(file, &text)
        }
    } else {
        eprintln!("No input file")
    }
}
