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

class TBinaryExpression(val left: TExpression, val right: TExpression, val operator: TBinaryOperator, location: Location) :
    TExpression(operator.resultType, listOf(left, right), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitBinaryExpression(this)
    }
}

enum class TBinaryOperator(val argType: Type, val resultType: Type = argType) {
    PLUS(Type.NUMBER),
    CONCAT(Type.STRING),
    MINUS(Type.NUMBER),
    MULTIPLY(Type.NUMBER),
    DIVIDE(Type.NUMBER),
    MODULO(Type.NUMBER),

    EQUALS(Type.DYNAMIC, Type.BOOLEAN),
    NOT_EQUALS(Type.DYNAMIC, Type.BOOLEAN),
    GREATER_THAN(Type.NUMBER, Type.BOOLEAN),
    LESS_THAN(Type.NUMBER, Type.BOOLEAN),
    GREATER_THAN_OR_EQUALS(Type.NUMBER, Type.BOOLEAN),
    LESS_THAN_OR_EQUALS(Type.NUMBER, Type.BOOLEAN),

    AND(Type.BOOLEAN, Type.BOOLEAN),
    OR(Type.BOOLEAN, Type.BOOLEAN),

    BITWISE_AND(Type.NUMBER, Type.NUMBER),
    BITWISE_OR(Type.NUMBER, Type.NUMBER),
    BITWISE_XOR(Type.NUMBER, Type.NUMBER),
    BITWISE_SHIFT_LEFT(Type.NUMBER, Type.NUMBER),
    BITWISE_SHIFT_RIGHT(Type.NUMBER, Type.NUMBER);
}

class TPrefixExpression(val right: TExpression, val operator: TPrefixOperator, location: Location) :
    TExpression(operator.resultType, listOf(right), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitPrefixExpression(this)
    }
}

enum class TPrefixOperator(val argType: Type, val resultType: Type = argType) {
    NOT(Type.BOOLEAN),
    // bitwise not is always replaced with a subtraction
    INC(Type.NUMBER),
    DEC(Type.NUMBER),
    MINUS(Type.NUMBER);
}