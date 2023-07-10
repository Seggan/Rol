use std::fmt::Debug;

use enumset::enum_set;
use enumset::EnumSet;
use pest::{Parser, RuleType};
use pest::iterators::{Pair, Pairs};
use pest::pratt_parser::{Assoc, Op, PrattParser};
use pest_derive::Parser;

use crate::common::Identifier;
use crate::error::RolError;
use crate::parsing::ast::{AstNode, Expr, Modifier};

#[derive(Parser)]
#[grammar = "parsing/rol_parser.pest"]
struct RolParser;

pub fn parse(code: &str) -> AstNode<()> {
    let file = RolParser::parse(Rule::file, code).unwrap().next().unwrap();
    parse_pair(file).pop().unwrap()
}

fn parse_pair(pair: Pair<Rule>) -> Vec<AstNode<()>> {
    let rule = pair.as_rule();
    let span = pair.as_span();
    match rule {
        Rule::file | Rule::statements => {
            let statements = pair.into_inner().map(parse_pair).flatten().collect::<Vec<_>>();
            vec![AstNode::Statements(statements, ())]
        }
        Rule::assignment => {
            let mut inner = pair.into_inner();
            let modifier = inner.first_node(Rule::variableStarter).map(|p| p.as_str());
            let name = inner.first_node(Rule::qname).unwrap().as_str().parse::<Identifier>().unwrap();
            let expr = parse_expr(pair.into_inner().nth(1).unwrap());
            if let Some(modifier) = modifier {
                if name.package.is_some() {
                    RolError::IllegalPackage.report();
                }
                let modifier = match modifier {
                    "val " => enum_set![Modifier::Final],
                    "var " => enum_set![],
                    _ => unreachable!()
                };
                vec![AstNode::VarDecl(modifier, name, ())]
            } else {
                vec![AstNode::VarAssign(name, expr, ())]
            }
        }
        Rule::expr => vec![AstNode::Expr(parse_expr(pair))],
        _ => panic!("Unexpected rule: {:?}", rule)
    }
}


static PRATT: PrattParser<Rule> = PrattParser::new()
    .op(Op::infix(Rule::or, Assoc::Left))
    .op(Op::infix(Rule::and, Assoc::Left))
    .op(Op::infix(Rule::eq, Assoc::Left) | Op::infix(Rule::neq, Assoc::Left))
    .op(Op::infix(Rule::lt, Assoc::Left)
        | Op::infix(Rule::gt, Assoc::Left)
        | Op::infix(Rule::leq, Assoc::Left)
        | Op::infix(Rule::geq, Assoc::Left)
    )
    .op(Op::infix(Rule::add, Assoc::Left) | Op::infix(Rule::sub, Assoc::Left))
    .op(Op::infix(Rule::mul, Assoc::Left)
        | Op::infix(Rule::div, Assoc::Left)
        | Op::infix(Rule::modulus, Assoc::Left)
    )
    .op(Op::infix(Rule::pow, Assoc::Right))
    .op(Op::prefix(Rule::neg) | Op::prefix(Rule::not))
    .op(Op::postfix(Rule::call) | Op::postfix(Rule::index) | Op::postfix(Rule::assertNotNull));

fn parse_expr(pair: Pair<Rule>) -> Expr<()> {
    assert_eq!(pair.as_rule(), Rule::expr);
    PRATT
        .map_primary(parse_primary)
        .map_prefix(parse_prefix)
        .map_infix(parse_infix)
        .map_postfix(parse_postfix)
        .parse(pair.into_inner())
}

fn parse_primary(expr: Pair<Rule>) -> Expr<()> {
    todo!()
}

fn parse_prefix(op: Pair<Rule>, expr: Expr<()>) -> Expr<()> {
    todo!()
}

fn parse_infix(lhs: Expr<()>, op: Pair<Rule>, rhs: Expr<()>) -> Expr<()> {
    todo!()
}

fn parse_postfix(expr: Expr<()>, op: Pair<Rule>) -> Expr<()> {
    todo!()
}

/// Converts a Pair into a more-or-less human-readable tree structure
fn simple_tree(pair: Pair<Rule>) -> String {
    let rule = pair.as_rule();
    let base_rule = match rule {
        Rule::name | Rule::qname | Rule::number | Rule::boolean => format!("{:?}[{}]", rule, pair.as_str()),
        _ => format!("{:?}", rule),
    };
    let sub_rules = pair.into_inner().map(simple_tree).collect::<Vec<String>>();
    if sub_rules.is_empty() {
        base_rule
    } else {
        format!("{}({})", base_rule, sub_rules.join(", "))
    }
}

trait PairsExt<T> {
    fn first_node(&mut self, rule: T) -> Option<Pair<T>>;
}

impl<T: RuleType + Ord> PairsExt<T> for Pairs<'_, T> {
    fn first_node(&mut self, rule: T) -> Option<Pair<T>> {
        self.find(|p| p.as_rule() == rule)
    }
}