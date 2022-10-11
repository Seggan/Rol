package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.AccessModifier
import io.github.seggan.rol.tree.Location

sealed class TVar(val name: String, type: Type, children: List<TNode>, location: Location) : TNode(type, children, location)

class TVarDef(name: String, type: Type, val access: AccessModifier, location: Location) :
    TVar(name, type, listOf(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitVariableDeclaration(this)
    }
}

class TVarAssign(name: String, type: Type, val value: TExpression, location: Location) :
    TVar(name, type, listOf(value), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitVariableAssignment(this)
    }
}