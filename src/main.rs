use std::env;
use std::fs::read_to_string;
use std::path::Path;

mod parsing;
mod common;

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if let Some(file) = args.first() {
        let text = r#"abc"#;
        println!("{:?}", parsing::parse(&text));
    } else {
        eprintln!("No input file")
    }
}
