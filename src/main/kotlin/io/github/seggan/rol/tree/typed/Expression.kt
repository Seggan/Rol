package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.AnyType
import io.github.seggan.rol.tree.common.ConcreteType
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type

sealed class TExpression(type: Type, subexpressions: List<TExpression>, location: Location) :
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
    PLUS("+", ConcreteType.NUMBER),
    CONCAT("..", ConcreteType.STRING),
    MINUS("-", ConcreteType.NUMBER),
    MULTIPLY("*", ConcreteType.NUMBER),
    DIVIDE("/", ConcreteType.NUMBER),
    MODULO("%", ConcreteType.NUMBER),

    EQUALS("==", AnyType, ConcreteType.BOOLEAN),
    NOT_EQUALS("~=", AnyType, ConcreteType.BOOLEAN),
    GREATER_THAN(">", ConcreteType.NUMBER, ConcreteType.BOOLEAN),
    LESS_THAN("<", ConcreteType.NUMBER, ConcreteType.BOOLEAN),
    GREATER_THAN_OR_EQUALS(">=", ConcreteType.NUMBER, ConcreteType.BOOLEAN),
    LESS_THAN_OR_EQUALS("<=", ConcreteType.NUMBER, ConcreteType.BOOLEAN),

    AND("and", ConcreteType.BOOLEAN, ConcreteType.BOOLEAN),
    OR("or", ConcreteType.BOOLEAN, ConcreteType.BOOLEAN),

    BITWISE_AND("bitwiseAnd", ConcreteType.NUMBER, ConcreteType.NUMBER),
    BITWISE_OR("bitwiseOr", ConcreteType.NUMBER, ConcreteType.NUMBER),
    BITWISE_XOR("bitwiseXor", ConcreteType.NUMBER, ConcreteType.NUMBER),
    BITWISE_SHIFT_LEFT("bitwiseLeftShift", ConcreteType.NUMBER, ConcreteType.NUMBER),
    BITWISE_SHIFT_RIGHT("bitwiseRightShift", ConcreteType.NUMBER, ConcreteType.NUMBER);
}

class TPrefixExpression(val right: TExpression, val operator: TPrefixOperator, location: Location) :
    TExpression(operator.resultType, listOf(right), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitPrefixExpression(this)
    }
}

enum class TPrefixOperator(val op: String, val argType: Type, val resultType: Type = argType) {
    NOT("not ", ConcreteType.BOOLEAN),

    // bitwise not is always replaced with a subtraction
    INC("", ConcreteType.NUMBER),
    DEC("", ConcreteType.NUMBER),
    MINUS("-", ConcreteType.NUMBER);
}

class TPostfixExpression(val left: TExpression, val operator: TPostfixOperator, type: Type, location: Location) :
    TExpression(type, listOf(left), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitPostfixExpression(this)
    }
}

enum class TPostfixOperator(val typer: (Type) -> Type?, val expect: Type) {
    INC({ if (it == ConcreteType.NUMBER) ConcreteType.NUMBER else null }, ConcreteType.NUMBER),
    DEC({ if (it == ConcreteType.NUMBER) ConcreteType.NUMBER else null }, ConcreteType.NUMBER),
    ASSERT_NON_NULL({ it.nonNullable() }, AnyType);
}