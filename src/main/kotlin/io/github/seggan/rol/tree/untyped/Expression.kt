package io.github.seggan.rol.tree.untyped

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class Expression(children: List<Node>) : Node(children)

@OptIn(ExperimentalContracts::class)
fun Node.asExpr(): Expression {
    contract {
        returns() implies (this@asExpr is Expression)
    }
    if (this is Expression) {
        return this
    } else {
        throw IllegalArgumentException("Node $this is not an expression")
    }
}

class BinaryExpression(val left: Expression, val right: Expression, val type: BinaryOperator) :
    Expression(listOf(left, right)) {
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

class PrefixExpression(val expr: Expression, val type: PrefixOperator) : Expression(listOf(expr)) {
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

class PostfixExpression(val expr: Expression, val type: PostfixOperator) : Expression(listOf(expr)) {
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