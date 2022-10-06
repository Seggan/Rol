package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.Location
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class UExpression(children: List<UNode>, location: Location) : UNode(children, location)

fun UNode.asExpr(): UExpression {
    if (this is UExpression) {
        return this
    } else {
        throw IllegalArgumentException("Node $this is not an expression")
    }
}

class UBinaryExpression(val left: UExpression, val right: UExpression, val type: BinaryOperator, location: Location) :
    UExpression(listOf(left, right), location) {
    override fun toString(): String {
        return "BinaryExpression($left $type $right)"
    }
}

enum class BinaryOperator(private val symbol: String) {
    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    DIVIDE("/"),
    MODULO("%"),

    EQUALS("=="),
    NOT_EQUALS("!="),
    LESS("<"),
    LESS_EQUALS("<="),
    GREATER(">"),
    GREATER_EQUALS(">="),
    AND("&&"),
    OR("||"),

    BITWISE_AND("&"),
    BITWISE_OR("|"),
    XOR("^"),
    SHIFT_LEFT("<<"),
    SHIFT_RIGHT(">>");

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): BinaryOperator = values().first { it.symbol == symbol }
    }
}

class UPrefixExpression(val expr: UExpression, val type: PrefixOperator, location: Location) :
    UExpression(listOf(expr), location) {
    override fun toString(): String {
        return "PrefixExpression($type $expr)"
    }
}

enum class PrefixOperator(private val symbol: String) {
    NOT("!"),
    BITWISE_NOT("~"),
    MINUS("-"),
    INC("++"),
    DEC("--");

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): PrefixOperator = values().first { it.symbol == symbol }
    }
}

class UPostfixExpression(val expr: UExpression, val type: PostfixOperator, location: Location) :
    UExpression(listOf(expr), location) {
    override fun toString(): String {
        return "PostfixExpression($expr $type)"
    }
}

enum class PostfixOperator(private val symbol: String) {
    INC("++"),
    DEC("--");

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): PostfixOperator = values().first { it.symbol == symbol }
    }
}