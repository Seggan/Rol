use std::env;
use std::fs::read_to_string;
use std::path::Path;

use antlr_rust::common_token_stream::CommonTokenStream;
use antlr_rust::InputStream;

use crate::antlr::lexer::RolLexer;
use crate::antlr::parser::RolParser;

mod antlr;

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if let Some(file) = args.first() {
        let text = read_to_string(Path::new(file)).unwrap();
        let lexer = RolLexer::new(InputStream::new(&text));
        let parser = RolParser::new(CommonTokenStream::new(lexer));
    } else {
        eprintln!("No input file")
    }
}
