package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.AccessModifier
import io.github.seggan.rol.tree.Location
import io.github.seggan.rol.tree.Reference

sealed class UVar(val name: String, children: List<UNode>, location: Location) : UNode(children, location)

class UVarDef(name: String, val constant: Boolean, val type: UTypename?, val access: AccessModifier, location: Location) :
    UVar(name, listOf(), location) {
    override fun toString(): String {
        return "VarDef($access, $name, constant=$constant, type=$type)"
    }
}

class UVarAssign(name: String, val value: UExpression, location: Location) :
    UVar(name, listOf(value), location), Reference {
    override fun toString(): String {
        return "VarAssign($name, $value)"
    }
}

enum class AssignType(private val symbol: String, val operation: UBinaryOperator?) {
    ASSIGN("=", null),
    PLUS_ASSIGN("+=", UBinaryOperator.PLUS),
    MINUS_ASSIGN("-=", UBinaryOperator.MINUS),
    TIMES_ASSIGN("*=", UBinaryOperator.TIMES),
    DIVIDE_ASSIGN("/=", UBinaryOperator.DIVIDE),
    MODULO_ASSIGN("%=", UBinaryOperator.MODULO),
    BITWISE_AND_ASSIGN("&=", UBinaryOperator.BITWISE_AND),
    BITWISE_OR_ASSIGN("|=", UBinaryOperator.BITWISE_OR),
    BITWISE_XOR_ASSIGN("^=", UBinaryOperator.XOR),
    BITWISE_LEFT_SHIFT_ASSIGN("<<=", UBinaryOperator.SHIFT_LEFT),
    BITWISE_RIGHT_SHIFT_ASSIGN(">>=", UBinaryOperator.SHIFT_RIGHT);

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): AssignType = values().first { it.symbol == symbol }
    }
}