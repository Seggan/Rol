package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.Location

sealed class TExpression(type: Type, val subexpressions: List<TExpression>, location: Location) :
    TNode(type, subexpressions, location)

fun TNode.asExpr(): TExpression {
    if (this is TExpression) {
        return this
    } else {
        throw IllegalArgumentException("Node $this is not an expression")
    }
}

class TBinaryExpression(
    val left: TExpression,
    val right: TExpression,
    val operator: TBinaryOperator,
    location: Location
) :
    TExpression(operator.resultType, listOf(left, right), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitBinaryExpression(this)
    }
}

enum class TBinaryOperator(val op: String, val argType: Type, val resultType: Type = argType) {
    PLUS("+", Type.NUMBER),
    CONCAT("..", Type.STRING),
    MINUS("-", Type.NUMBER),
    MULTIPLY("*", Type.NUMBER),
    DIVIDE("/", Type.NUMBER),
    MODULO("%", Type.NUMBER),

    EQUALS("==", Type.DYNAMIC, Type.BOOLEAN),
    NOT_EQUALS("~=", Type.DYNAMIC, Type.BOOLEAN),
    GREATER_THAN(">", Type.NUMBER, Type.BOOLEAN),
    LESS_THAN("<", Type.NUMBER, Type.BOOLEAN),
    GREATER_THAN_OR_EQUALS(">=", Type.NUMBER, Type.BOOLEAN),
    LESS_THAN_OR_EQUALS("<=", Type.NUMBER, Type.BOOLEAN),

    AND("and", Type.BOOLEAN, Type.BOOLEAN),
    OR("or", Type.BOOLEAN, Type.BOOLEAN),

    BITWISE_AND("bitwiseAnd", Type.NUMBER, Type.NUMBER),
    BITWISE_OR("bitwiseOr", Type.NUMBER, Type.NUMBER),
    BITWISE_XOR("bitwiseXor", Type.NUMBER, Type.NUMBER),
    BITWISE_SHIFT_LEFT("bitwiseLeftShift", Type.NUMBER, Type.NUMBER),
    BITWISE_SHIFT_RIGHT("bitwiseRightShift", Type.NUMBER, Type.NUMBER);
}

class TPrefixExpression(val right: TExpression, val operator: TPrefixOperator, location: Location) :
    TExpression(operator.resultType, listOf(right), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitPrefixExpression(this)
    }
}

enum class TPrefixOperator(val op: String, val argType: Type, val resultType: Type = argType) {
    NOT("not ", Type.BOOLEAN),

    // bitwise not is always replaced with a subtraction
    INC("", Type.NUMBER),
    DEC("", Type.NUMBER),
    MINUS("-", Type.NUMBER);
}

class TPostfixExpression(val left: TExpression, val operator: TPostfixOperator, type: Type, location: Location) :
    TExpression(type, listOf(left), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitPostfixExpression(this)
    }
}

enum class TPostfixOperator(val typer: (Type) -> Type?, val expect: Type) {
    INC({ if (it == Type.NUMBER) Type.NUMBER else null }, Type.NUMBER),
    DEC({ if (it == Type.NUMBER) Type.NUMBER else null }, Type.NUMBER),
    ASSERT_NON_NULL({ it.copy(nullable = false) }, Type.ANY);
}