use pest_derive::Parser;

#[derive(Parser)]
#[grammar = "parsing/rol_parser.pest"]
struct RolParser;

pub fn parse(code: &str) {
}