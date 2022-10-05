package io.github.seggan.rol.tree.untyped

sealed class Literal : Expression(listOf())

class NumberLiteral(val value: Double) : Literal() {
    override fun toString() = value.toString()
}

class StringLiteral(val value: String) : Literal() {
    override fun toString() = "\"$value\""
}

class BooleanLiteral(val value: Boolean) : Literal() {
    override fun toString() = value.toString()
}

object NullLiteral : Literal() {
    override fun toString() = "null (literal)"
}