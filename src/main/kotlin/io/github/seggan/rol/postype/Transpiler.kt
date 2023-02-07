package io.github.seggan.rol.postype

import io.github.seggan.rol.resolution.DependencyManager
import io.github.seggan.rol.resolution.TypeResolver
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.lua.LAssignment
import io.github.seggan.rol.tree.lua.LBinaryExpression
import io.github.seggan.rol.tree.lua.LExpression
import io.github.seggan.rol.tree.lua.LFunction
import io.github.seggan.rol.tree.lua.LFunctionCall
import io.github.seggan.rol.tree.lua.LIfStatement
import io.github.seggan.rol.tree.lua.LLiteral
import io.github.seggan.rol.tree.lua.LNode
import io.github.seggan.rol.tree.lua.LNop
import io.github.seggan.rol.tree.lua.LReturn
import io.github.seggan.rol.tree.lua.LStatements
import io.github.seggan.rol.tree.lua.LString
import io.github.seggan.rol.tree.lua.LUnaryExpression
import io.github.seggan.rol.tree.lua.LVariableDeclaration
import io.github.seggan.rol.tree.typed.TAccess
import io.github.seggan.rol.tree.typed.TBinaryExpression
import io.github.seggan.rol.tree.typed.TBinaryOperator
import io.github.seggan.rol.tree.typed.TBoolean
import io.github.seggan.rol.tree.typed.TCall
import io.github.seggan.rol.tree.typed.TExpression
import io.github.seggan.rol.tree.typed.TIfStatement
import io.github.seggan.rol.tree.typed.TLambda
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.typed.TNull
import io.github.seggan.rol.tree.typed.TNumber
import io.github.seggan.rol.tree.typed.TPostfixExpression
import io.github.seggan.rol.tree.typed.TPostfixOperator
import io.github.seggan.rol.tree.typed.TPrefixExpression
import io.github.seggan.rol.tree.typed.TReturn
import io.github.seggan.rol.tree.typed.TStatements
import io.github.seggan.rol.tree.typed.TString
import io.github.seggan.rol.tree.typed.TVarAssign
import io.github.seggan.rol.tree.typed.TVarDef
import io.github.seggan.rol.tree.typed.TVariableAccess
import io.github.seggan.rol.tree.typed.TypedTreeVisitor
import java.time.LocalTime
import java.util.EnumSet

class Transpiler(
    private val resolver: TypeResolver,
) : TypedTreeVisitor<LNode>() {

    private var indent = 0

    override fun start(node: TNode): LNode {
        return visit(node)
    }

    override fun defaultValue(node: TNode): LNode {
        return LNop
    }

    override fun visitStatements(statements: TStatements): LNode {
        return LStatements(statements.children.map(::visit))
    }

    override fun visitPrefixExpression(expression: TPrefixExpression): LNode {
        return LUnaryExpression(expression.operator.op, visit(expression.right))
    }

    override fun visitBinaryExpression(expression: TBinaryExpression): LNode {
        val op = expression.operator
        return if (op in bitwiseOps) {
            LFunctionCall(
                LLiteral(op.op),
                visit(expression.left) as LExpression,
                visit(expression.right) as LExpression
            )
        } else {
            LBinaryExpression(visit(expression.left), op.op, visit(expression.right))
        }
    }

    override fun visitPostfixExpression(expression: TPostfixExpression): LNode {
        if (expression.operator == TPostfixOperator.ASSERT_NON_NULL) {
            return LFunctionCall(
                LLiteral("assertNonNull"),
                visitExpression(expression.left),
                LString(expression.location.toString().replace("\"", "\\\""))
            )
        }
        return super.visitPostfixExpression(expression)
    }

    override fun visitNumber(num: TNumber): LNode {
        return LLiteral(num.value.stripTrailingZeros().toPlainString())
    }

    override fun visitString(string: TString): LNode {
        return LString(string.value)
    }

    override fun visitBoolean(bool: TBoolean): LNode {
        return LLiteral(bool.value.toString())
    }

    override fun visitNull(tNull: TNull): LNode {
        return LLiteral("nil")
    }

    override fun visitFunctionCall(call: TCall): LNode {
        return LFunctionCall(
            visitExpression(call.expr),
            call.args.map(::visitExpression)
        )
    }

    override fun visitVariableDeclaration(declaration: TVarDef): LNode {
        return LVariableDeclaration(mangle(declaration.name, declaration.type))
    }

    override fun visitVariableAccess(access: TVariableAccess): LNode {
        return LLiteral(resolver.mangledVariables[access.field] ?: mangle(access.field, access.type))
    }

    override fun visitVariableAssignment(assignment: TVarAssign): LNode {
        return LAssignment(
            resolver.mangledVariables[assignment.name] ?: mangle(assignment.name, assignment.type),
            visit(assignment.value)
        )
    }

    override fun visitReturn(ret: TReturn): LNode {
        return LReturn(if (ret.value == null) null else visit(ret.value))
    }

    override fun visitLambda(lambda: TLambda): LNode {
        return LFunction(
            lambda.args.map { mangle(Identifier(it.name), it.type) },
            visit(lambda.body).toStatements().withIndent(++indent)
        ).also { indent-- }
    }

    override fun visitAccess(access: TAccess): LNode {
        return LBinaryExpression(visit(access.target), ".", LLiteral(access.field.name))
    }

    override fun visitIfStatement(statement: TIfStatement): LNode {
        return LIfStatement(
            visit(statement.condition) as LExpression,
            visit(statement.ifBody).toStatements().withIndent(++indent),
            if (statement.elseBody == null) null else visit(statement.elseBody).toStatements().withIndent(indent)
        ).also { indent-- }
    }

    private fun visitExpression(expression: TExpression): LExpression {
        return visit(expression) as LExpression
    }
}

private val bitwiseOps = EnumSet.of(
    TBinaryOperator.BITWISE_AND,
    TBinaryOperator.BITWISE_OR,
    TBinaryOperator.BITWISE_XOR,
    TBinaryOperator.BITWISE_SHIFT_LEFT,
    TBinaryOperator.BITWISE_SHIFT_RIGHT,
)

private val regex = "\\W".toRegex()

private fun mangle(type: Type): String {
    return regex.replace(type.hashCode().toString(16).take(6), "_")
}

private fun mangle(name: Identifier, type: Type): String {
    return regex.replace(name.toString(), "_") + mangle(type)
}

private fun LNode.toStatements(): LStatements {
    return if (this is LStatements) this else LStatements(listOf(this))
}