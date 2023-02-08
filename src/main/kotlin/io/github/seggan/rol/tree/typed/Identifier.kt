package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type

sealed class TIdentifier(val field: Identifier, type: Type, children: List<TExpression>, location: Location) :
    TExpression(type, children, location)

class TVariableAccess(name: Identifier, type: Type, location: Location) : TIdentifier(name, type, emptyList(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitVariableAccess(this)
    }
}

class TCall(val expr: TExpression, val args: List<TExpression>, type: Type, location: Location) :
    TExpression(type, args, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitFunctionCall(this)
    }
}

class TAccess(val target: TExpression, name: String, type: Type, location: Location) :
    TIdentifier(Identifier(name), type, listOf(target), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitAccess(this)
    }
}