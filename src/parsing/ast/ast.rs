use std::fmt::{Debug, Display, Formatter};

use enumset::{EnumSet, EnumSetType};

use crate::parsing::common::Identifier;

use super::Expr;

#[derive(Debug)]
pub enum AstNode<E: Debug> {
    Statements(Vec<AstNode<E>>, E),
    VarDecl(EnumSet<Modifier>, Identifier, E),
    VarAssign(Identifier, Expr<E>, E),
    Expr(Expr<E>),
}

impl<E: Debug> AstNode<E> {
    pub fn extra_data(&self) -> &E {
        match self {
            AstNode::Statements(_, e) => e,
            AstNode::VarDecl(_, _, e) => e,
            AstNode::VarAssign(_, _, e) => e,
            AstNode::Expr(e) => e.extra_data(),
        }
    }
}

impl<T: Debug> Display for AstNode<T> {
    fn fmt(&self, f: &mut Formatter<'_>) -> std::fmt::Result {
        match self {
            AstNode::Statements(stmts, _) => {
                let stmts = stmts.iter().map(|s| format!("{}", s)).collect::<Vec<_>>().join("; ");
                write!(f, "{{ {} }}", stmts)
            }
            AstNode::VarDecl(modifiers, name, _) => {
                let modifiers = modifiers.iter().map(|m| format!("{:?}", m)).collect::<Vec<_>>().join(" ");
                write!(f, "VarDecl({}, {})", modifiers, name)
            },
            AstNode::VarAssign(name, expr, _) => write!(f, "{} = {}", name, expr),
            AstNode::Expr(expr) => write!(f, "{}", expr),
        }
    }
}

#[derive(Debug, EnumSetType)]
pub enum Modifier {
    Public,
    Private,
    Package,
    Final,
}
