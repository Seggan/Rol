package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.AccessModifier
import io.github.seggan.rol.tree.Location

sealed class UVar(val name: String, children: List<UNode>, location: Location) : UNode(children, location)

class UVarDef(name: String, val constant: Boolean, val type: UTypename?, val access: AccessModifier, location: Location) :
    UVar(name, listOf(), location) {
    override fun toString(): String {
        return "VarDef($access, $name, constant=$constant, type=$type)"
    }
}

class UVarAssign(name: String, val value: UExpression, location: Location) : UVar(name, listOf(value), location) {
    override fun toString(): String {
        return "VarAssign($name, $value)"
    }
}

enum class AssignType(private val symbol: String, val operation: BinaryOperator?) {
    ASSIGN("=", null),
    PLUS_ASSIGN("+=", BinaryOperator.PLUS),
    MINUS_ASSIGN("-=", BinaryOperator.MINUS),
    TIMES_ASSIGN("*=", BinaryOperator.TIMES),
    DIVIDE_ASSIGN("/=", BinaryOperator.DIVIDE),
    MODULO_ASSIGN("%=", BinaryOperator.MODULO),
    BITWISE_AND_ASSIGN("&=", BinaryOperator.BITWISE_AND),
    BITWISE_OR_ASSIGN("|=", BinaryOperator.BITWISE_OR),
    BITWISE_XOR_ASSIGN("^=", BinaryOperator.XOR),
    BITWISE_LEFT_SHIFT_ASSIGN("<<=", BinaryOperator.SHIFT_LEFT),
    BITWISE_RIGHT_SHIFT_ASSIGN(">>=", BinaryOperator.SHIFT_RIGHT);

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): AssignType = values().first { it.symbol == symbol }
    }
}