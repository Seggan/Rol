use std::env;
use std::fs::{read, read_to_string};
use std::path::Path;

mod parsing;
mod common;
mod error;

fn main() {
    let args = env::args().collect::<Vec<String>>();
    if let Some(file) = args.get(1) {
        println!("Reading file: {}", file);
        let text = read_to_string(file).unwrap();
        println!("{}", parsing::parse(&text));
    } else {
        eprintln!("No input file")
    }
}
