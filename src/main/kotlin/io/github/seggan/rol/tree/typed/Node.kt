package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.common.VoidType

sealed class TNode(val type: Type, val children: List<TNode>, val location: Location) {
    abstract fun <T> accept(visitor: TypedTreeVisitor<T>): T
}

class TStatements(children: List<TNode>, location: Location) : TNode(VoidType, children, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitStatements(this)
    }
}