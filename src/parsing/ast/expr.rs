use std::fmt::Debug;

use crate::common::Identifier;

#[derive(Debug)]
pub enum Expr<E: Debug> {
    BinOp(Box<Expr<E>>, BinOp, Box<Expr<E>>, E),
    PrefixOp(PrefixOp, Box<Expr<E>>, E),
    PostfixOp(Box<Expr<E>>, PostfixOp<E>, E),
    VarAccess(Identifier, E)
}

#[derive(Debug)]
pub enum BinOp {
    Plus,
    Minus,
    Times,
    Divide,
    Mod,
    Pow
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