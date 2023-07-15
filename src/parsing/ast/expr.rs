use std::fmt::{Debug, Display, Formatter};

use crate::parsing::common::Identifier;

#[derive(Debug)]
pub enum Expr<E: Debug> {
    BinOp(Box<Expr<E>>, BinOp, Box<Expr<E>>, E),
    PrefixOp(PrefixOp, Box<Expr<E>>, E),
    PostfixOp(Box<Expr<E>>, PostfixOp<E>, E),
    VarAccess(Identifier, E),
    Literal(Literal, E),
}

impl<T: Debug> Display for Expr<T> {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Expr::BinOp(lhs, op, rhs, _) => write!(f, "({} {:?} {})", lhs, op, rhs),
            Expr::PrefixOp(op, expr, _) => write!(f, "({:?}{})", op, expr),
            Expr::PostfixOp(expr, op, _) => write!(f, "({}{})", expr, op),
            Expr::VarAccess(name, _) => write!(f, "{}", name),
            Expr::Literal(lit, _) => write!(f, "{}", lit),
        }
    }
}

impl<T: Debug> Expr<T> {
    pub fn extra_data(&self) -> &T {
        match self {
            Expr::BinOp(_, _, _, data) => data,
            Expr::PrefixOp(_, _, data) => data,
            Expr::PostfixOp(_, _, data) => data,
            Expr::VarAccess(_, data) => data,
            Expr::Literal(_, data) => data,
        }
    }
}

#[derive(Debug)]
pub enum BinOp {
    Plus,
    Minus,
    Times,
    Divide,
    Modulo,
    Power,

    Equals,
    NotEquals,
    And,
    Or,
    LessThan,
    LessThanOrEqual,
    GreaterThan,
    GreaterThanOrEqual
}

#[derive(Debug)]
pub enum PrefixOp {
    Negate,
    Not
}

#[derive(Debug)]
pub enum PostfixOp<E: Debug> {
    FunctionCall(Vec<Expr<E>>),
    Index(Box<Expr<E>>),
    AssertNotNull
}

impl<E: Debug> Display for PostfixOp<E> {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            PostfixOp::FunctionCall(args) => {
                let args = args.iter().map(|a| format!("{}", a)).collect::<Vec<_>>().join(", ");
                write!(f, "({})", args)
            },
            PostfixOp::Index(expr) => write!(f, "[{}]", expr),
            PostfixOp::AssertNotNull => write!(f, "!"),
        }
    }
}

#[derive(Debug)]
pub enum Literal {
    Int(i64),
    Float(String),
    String(String),
    Bool(bool),
    Null
}

impl Display for Literal {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            Literal::Int(i) => write!(f, "{}", i),
            Literal::Float(s) => write!(f, "{}", s),
            Literal::String(s) => write!(f, "\"{}\"", s),
            Literal::Bool(b) => write!(f, "{}", b),
            Literal::Null => write!(f, "null"),
        }
    }
}