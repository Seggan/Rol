package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.Location

sealed class TNode(val type: Type, val children: List<TNode>, val location: Location) {
    abstract fun <T> accept(visitor: TypedTreeVisitor<T>): T
}

class TStatements(children: List<TNode>, location: Location) : TNode(Type.DYNAMIC, children, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitStatements(this)
    }
}