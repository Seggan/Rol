use pest::iterators::Pair;
use pest::Parser;
use pest_derive::Parser;

#[derive(Parser)]
#[grammar = "parsing/rol_parser.pest"]
struct RolParser;

pub fn parse(code: &str) -> Vec<Pair<Rule>> {
    let pairs = RolParser::parse(Rule::file, code).unwrap().collect::<Vec<_>>();
    pairs
}