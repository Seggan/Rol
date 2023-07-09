use std::fmt::{Debug, format};

use pest::iterators::Pair;
use pest::Parser;
use pest_derive::Parser;

#[derive(Parser)]
#[grammar = "parsing/rol_parser.pest"]
struct RolParser;

pub fn parse(code: &str) -> String {
    let file = RolParser::parse(Rule::file, code).unwrap().next().unwrap();
    simple_tree(file)
}

/// Converts a Pair into a more-or-less human-readable tree structure
fn simple_tree(pair: Pair<Rule>) -> String {
    let rule = pair.as_rule();
    let base_rule = match rule {
        Rule::identifier | Rule::number | Rule::boolean => format!("{:?}[{}]", rule, pair.as_str()),
        _ => format!("{:?}", rule),
    };
    let sub_rules = pair.into_inner().map(simple_tree).collect::<Vec<String>>();
    if sub_rules.is_empty() {
        base_rule
    } else {
        format!("{}({})", base_rule, sub_rules.join(", "))
    }
}