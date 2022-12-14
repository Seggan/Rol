package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type

sealed class TIdentifier(val field: String, type: Type, children: List<TExpression>, location: Location) :
    TExpression(type, children, location)

class TVariableAccess(name: String, type: Type, location: Location) : TIdentifier(name, type, emptyList(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitVariableAccess(this)
    }
}

class TFunctionCall(val fname: Identifier, val args: List<TExpression>, returnType: Type, location: Location) :
    TIdentifier(fname.name, returnType, args, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitFunctionCall(this)
    }
}

class TAccess(val target: TExpression, name: String, type: Type, location: Location) :
    TIdentifier(name, type, listOf(target), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitAccess(this)
    }
}