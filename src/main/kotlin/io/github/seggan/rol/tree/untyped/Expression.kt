package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.typed.TBinaryOperator
import io.github.seggan.rol.tree.typed.TPostfixOperator
import io.github.seggan.rol.tree.typed.TPrefixOperator

sealed class UExpression(children: List<UNode>, location: Location) : UNode(children, location)

fun UNode.asExpr(): UExpression {
    if (this is UExpression) {
        return this
    } else {
        throw IllegalArgumentException("Node $this is not an expression")
    }
}

class UBinaryExpression(val left: UExpression, val right: UExpression, val type: UBinaryOperator, location: Location) :
    UExpression(listOf(left, right), location) {
    override fun toString(): String {
        return "BinaryExpression($left $type $right)"
    }
}

enum class UBinaryOperator(private val symbol: String, val typedOperator: TBinaryOperator?) {
    PLUS("+", null),
    MINUS("-", TBinaryOperator.MINUS),
    TIMES("*", TBinaryOperator.MULTIPLY),
    DIVIDE("/", TBinaryOperator.DIVIDE),
    MODULO("%", TBinaryOperator.MODULO),

    EQUALS("==", TBinaryOperator.EQUALS),
    NOT_EQUALS("!=", TBinaryOperator.NOT_EQUALS),
    LESS("<", TBinaryOperator.LESS_THAN),
    LESS_EQUALS("<=", TBinaryOperator.LESS_THAN_OR_EQUALS),
    GREATER(">", TBinaryOperator.GREATER_THAN),
    GREATER_EQUALS(">=", TBinaryOperator.GREATER_THAN_OR_EQUALS),
    AND("&&", TBinaryOperator.AND),
    OR("||", TBinaryOperator.OR),

    BITWISE_AND("&", TBinaryOperator.BITWISE_AND),
    BITWISE_OR("|", TBinaryOperator.BITWISE_OR),
    XOR("^", TBinaryOperator.BITWISE_XOR),
    SHIFT_LEFT("<<", TBinaryOperator.BITWISE_SHIFT_LEFT),
    SHIFT_RIGHT(">>", TBinaryOperator.BITWISE_SHIFT_RIGHT);

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): UBinaryOperator = values().first { it.symbol == symbol }
    }
}

class UPrefixExpression(val expr: UExpression, val type: UPrefixOperator, location: Location) :
    UExpression(listOf(expr), location) {
    override fun toString(): String {
        return "PrefixExpression($type $expr)"
    }
}

enum class UPrefixOperator(private val symbol: String, val typedOperator: TPrefixOperator?) {
    NOT("!", TPrefixOperator.NOT),
    BITWISE_NOT("~", null),
    MINUS("-", TPrefixOperator.MINUS),
    INC("++", TPrefixOperator.INC),
    DEC("--", TPrefixOperator.DEC);

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): UPrefixOperator = values().first { it.symbol == symbol }
    }
}

class UPostfixExpression(val expr: UExpression, val type: UPostfixOperator, location: Location) :
    UExpression(listOf(expr), location) {
    override fun toString(): String {
        return "PostfixExpression($expr $type)"
    }
}

enum class UPostfixOperator(private val symbol: String, val typedOperator: TPostfixOperator) {
    INC("++", TPostfixOperator.INC),
    DEC("--", TPostfixOperator.DEC),
    NOT_NULL("!", TPostfixOperator.ASSERT_NON_NULL);

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): UPostfixOperator = values().first { it.symbol == symbol }
    }
}