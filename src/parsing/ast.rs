use crate::common::Identifier;

pub enum AstNode<E> {
    Statements(Vec<AstNode<E>>, E),
    Expr(Expr<E>)
}

pub enum Expr<E> {
    BinOp(Box<Expr<E>>, Operator, Box<Expr<E>>, E),
    UnOp(Operator, Box<Expr<E>>, E),
    VarAccess(Identifier, E)
}

pub enum Operator {
    Plus
}