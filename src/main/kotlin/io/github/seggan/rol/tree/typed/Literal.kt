package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.Location

sealed class TLiteral(type: Type, location: Location) : TExpression(type, listOf(), location)

class TNumber(val value: Double, location: Location) : TLiteral(Type.NUMBER, location) {

    constructor(value: Int, location: Location) : this(value.toDouble(), location)

    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitNumber(this)
    }
}

class TString(val value: String, location: Location) : TLiteral(Type.STRING, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitString(this)
    }
}

class TBoolean(val value: Boolean, location: Location) : TLiteral(Type.BOOLEAN, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitBoolean(this)
    }
}

class TNull(location: Location) : TLiteral(Type.ANY, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitNull(this)
    }
}