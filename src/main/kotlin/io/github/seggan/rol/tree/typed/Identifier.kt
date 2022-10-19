package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type

sealed class TIdentifier(val name: String, type: Type, children: List<TExpression>, location: Location) :
    TExpression(type, children, location)

class TVariableAccess(name: String, type: Type, location: Location) : TIdentifier(name, type, emptyList(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitVariableAccess(this)
    }
}

class TFunctionCall(name: String, val args: List<TExpression>, returnType: Type, location: Location) :
    TIdentifier(name, returnType, args, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitFunctionCall(this)
    }
}

class TAccess(val obj: TExpression, name: String, type: Type, location: Location) :
    TIdentifier(name, type, listOf(obj), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        TODO("Not yet implemented")
    }
}