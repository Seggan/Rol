package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.Location

sealed class ULiteral(location: Location) : UExpression(listOf(), location)

class UNumberLiteral(val value: Double, location: Location) : ULiteral(location) {
    override fun toString() = value.toString()
}

class UStringLiteral(val value: String, location: Location) : ULiteral(location) {
    override fun toString() = "\"$value\""
}

class UBooleanLiteral(val value: Boolean, location: Location) : ULiteral(location) {
    override fun toString() = value.toString()
}

class UNullLiteral(location: Location) : ULiteral(location) {
    override fun toString() = "null (literal)"
}