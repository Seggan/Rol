package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type
import java.math.BigDecimal

sealed class TLiteral<T>(val value: T, type: Type, location: Location) : TExpression(type, listOf(), location)

class TNumber(value: BigDecimal, location: Location) : TLiteral<BigDecimal>(value, Type.NUMBER, location) {

    constructor(value: Int, location: Location) : this(value.toBigDecimal(), location)

    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitNumber(this)
    }
}

class TString(value: String, location: Location) : TLiteral<String>(value, Type.STRING, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitString(this)
    }
}

class TBoolean(value: Boolean, location: Location) : TLiteral<Boolean>(value, Type.BOOLEAN, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitBoolean(this)
    }
}

class TNull(location: Location) : TLiteral<Unit?>(null, Type.ANY, location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitNull(this)
    }
}