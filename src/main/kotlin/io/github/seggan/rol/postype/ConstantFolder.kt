package io.github.seggan.rol.postype

import io.github.seggan.rol.Errors
import io.github.seggan.rol.tree.typed.TBinaryExpression
import io.github.seggan.rol.tree.typed.TBinaryOperator
import io.github.seggan.rol.tree.typed.TBoolean
import io.github.seggan.rol.tree.typed.TExpression
import io.github.seggan.rol.tree.typed.TLiteral
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.typed.TNumber
import io.github.seggan.rol.tree.typed.TPostfixExpression
import io.github.seggan.rol.tree.typed.TPostfixOperator
import io.github.seggan.rol.tree.typed.TString

class ConstantFolder : Transformer() {

    var changed = false

    override fun visitPostfixExpression(expression: TPostfixExpression): TNode {
        if (expression.operator == TPostfixOperator.ASSERT_NON_NULL && !expression.left.type.nullable) {
            changed = true
            return expression.left
        }
        return expression
    }

    override fun visitBinaryExpression(expression: TBinaryExpression): TNode {
        // Arithmetic
        checkConstants<TNumber, TNumber>(expression, TBinaryOperator.PLUS) { a, b ->
            return TNumber(a.value + b.value, expression.location)
        }
        checkConstants<TNumber, TNumber>(expression, TBinaryOperator.MINUS) { a, b ->
            return TNumber(a.value - b.value, expression.location)
        }
        checkConstants<TNumber, TNumber>(expression, TBinaryOperator.MULTIPLY) { a, b ->
            return TNumber(a.value * b.value, expression.location)
        }
        checkConstants<TNumber, TNumber>(expression, TBinaryOperator.DIVIDE) { a, b ->
            try {
                return TNumber(a.value / b.value, expression.location)
            } catch (e: ArithmeticException) {
                Errors.genericError("Arithmetic", "Division by zero", expression.location)
            }
        }
        // String concat
        checkConstants<TString, TString>(expression, TBinaryOperator.CONCAT) { a, b ->
            return TString(a.value + b.value, expression.location)
        }
        // Boolean
        checkConstants<TLiteral<*>, TLiteral<*>>(expression, TBinaryOperator.EQUALS) { a, b ->
            return TBoolean(a.value == b.value, expression.location)
        }
        checkConstants<TLiteral<*>, TLiteral<*>>(expression, TBinaryOperator.NOT_EQUALS) { a, b ->
            return TBoolean(a.value != b.value, expression.location)
        }
        checkConstants<TBoolean, TExpression>(expression, TBinaryOperator.AND) { a, b ->
            if (!a.value) {
                return TBoolean(false, expression.location)
            } else if (b is TBoolean) {
                return TBoolean(b.value, expression.location)
            }
            changed = false
        }
        checkConstants<TBoolean, TExpression>(expression, TBinaryOperator.OR) { a, b ->
            if (a.value) {
                return TBoolean(true, expression.location)
            } else if (b is TBoolean) {
                return TBoolean(b.value, expression.location)
            }
            changed = false
        }
        return super.visitBinaryExpression(expression)
    }

    private inline fun <reified L, reified R> checkConstants(
        expression: TBinaryExpression,
        operator: TBinaryOperator,
        fn: (L, R) -> Unit
    ) {
        if (expression.operator == operator && expression.left is L && expression.right is R) {
            changed = true
            fn(expression.left as L, expression.right as R)
        }
    }
}