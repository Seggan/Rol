package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Type

sealed class TVar(val name: Identifier, type: Type, children: List<TNode>, location: Location) : TNode(type, children, location)

class TVarDef(name: Identifier, type: Type, val modifiers: Modifiers, location: Location) :
    TVar(name, type, listOf(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitVariableDeclaration(this)
    }
}

class TVarAssign(name: Identifier, type: Type, val value: TExpression, location: Location) :
    TVar(name, type, listOf(value), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitVariableAssignment(this)
    }
}