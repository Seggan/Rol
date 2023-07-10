use std::fmt::Debug;

use enumset::{EnumSet, EnumSetType};

pub use expr::*;

use crate::common::Identifier;

mod expr;

#[derive(Debug)]
pub enum AstNode<E: Debug> {
    Statements(Vec<AstNode<E>>, E),
    VarDecl(EnumSet<Modifier>, Identifier, E),
    VarAssign(Identifier, Expr<E>, E),
    Expr(Expr<E>),
}

#[derive(Debug, EnumSetType)]
pub enum Modifier {
    Public,
    Private,
    Package,
    Final,
}