use std::fmt::Debug;

use crate::common::Identifier;

#[derive(Debug)]
pub enum Expr<E: Debug> {
    BinOp(Box<Expr<E>>, BinOp, Box<Expr<E>>, E),
    PrefixOp(PrefixOp, Box<Expr<E>>, E),
    PostfixOp(Box<Expr<E>>, PostfixOp<E>, E),
    VarAccess(Identifier, E),
    Literal(Literal, E),
}

#[derive(Debug)]
pub enum BinOp {
    Plus,
    Minus,
    Times,
    Divide,
    Mod,
    Pow,

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
    Neg,
    Not
}

#[derive(Debug)]
pub enum PostfixOp<E: Debug> {
    FunctionCall(Vec<Expr<E>>),
    Index(Box<Expr<E>>),
    AssertNotNull
}

#[derive(Debug)]
pub enum Literal {
    Int(i64),
    Float(f64),
    String(String),
    Bool(bool),
    Null
}