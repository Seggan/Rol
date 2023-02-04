package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Argument
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.common.VoidType

class TLambda(
    val args: List<Argument>,
    val body: TStatements,
    returnType: Type,
    location: Location
) : TExpression(returnType, listOf(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitLambda(this)
    }
}

class TReturn(val value: TExpression?, location: Location) :
    TNode(value?.type ?: VoidType, listOfNotNull(value), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitReturn(this)
    }
}