package io.github.seggan.rol.parsing

import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.antlr.RolParserBaseVisitor
import io.github.seggan.rol.tree.untyped.Access
import io.github.seggan.rol.tree.untyped.AssignType
import io.github.seggan.rol.tree.untyped.BinaryExpression
import io.github.seggan.rol.tree.untyped.BinaryOperator
import io.github.seggan.rol.tree.untyped.BooleanLiteral
import io.github.seggan.rol.tree.untyped.Expression
import io.github.seggan.rol.tree.untyped.FunctionCall
import io.github.seggan.rol.tree.untyped.Node
import io.github.seggan.rol.tree.untyped.NullLiteral
import io.github.seggan.rol.tree.untyped.NumberLiteral
import io.github.seggan.rol.tree.untyped.PostfixExpression
import io.github.seggan.rol.tree.untyped.PostfixOperator
import io.github.seggan.rol.tree.untyped.PrefixExpression
import io.github.seggan.rol.tree.untyped.PrefixOperator
import io.github.seggan.rol.tree.untyped.Statements
import io.github.seggan.rol.tree.untyped.StringLiteral
import io.github.seggan.rol.tree.untyped.Typename
import io.github.seggan.rol.tree.untyped.VarAssign
import io.github.seggan.rol.tree.untyped.VarDef
import io.github.seggan.rol.tree.untyped.VariableAccess
import io.github.seggan.rol.tree.untyped.asExpr

class RolVisitor : RolParserBaseVisitor<Node>() {

    override fun visitFile(ctx: RolParser.FileContext): Node {
        fun flattenStatements(node: Node): List<Node> =
            if (node is Statements) node.children.flatMap(::flattenStatements) else listOf(node)

        return Statements(visitChildren(ctx).children.flatMap(::flattenStatements))
    }

    override fun visitStatements(ctx: RolParser.StatementsContext): Node {
        return Statements(ctx.statement().map(::visit))
    }

    override fun visitExpression(ctx: RolParser.ExpressionContext): Node {
        return when {
            ctx.postfixOp != null -> PostfixExpression(
                visit(ctx.expression(0)).asExpr(),
                PostfixOperator.fromSymbol(ctx.postfixOp.text)
            )
            ctx.prefixOp != null -> PrefixExpression(
                visit(ctx.expression(0)).asExpr(),
                PrefixOperator.fromSymbol(ctx.prefixOp.text)
            )
            ctx.op != null -> BinaryExpression(
                visit(ctx.expression(0)).asExpr(),
                visit(ctx.expression(1)).asExpr(),
                BinaryOperator.fromSymbol(ctx.op.text)
            )
            ctx.identifier() != null -> Access(visit(ctx.expression(0)).asExpr(), ctx.identifier().text)
            ctx.call() != null -> {
                val call = visit(ctx.call()) as FunctionCall
                return FunctionCall(call.name, listOf(visit(ctx.expression(0)).asExpr()) + call.args)
            }
            else -> visitChildren(ctx)
        }
    }

    override fun visitNumber(ctx: RolParser.NumberContext): Node {
        val text = ctx.text
        val num = when {
            ctx.Number() != null -> text.toDouble()
            ctx.HexNumber() != null -> text.substring(2).toLong(16).toDouble()
            ctx.BinNumber() != null -> text.substring(2).toLong(2).toDouble()
            ctx.OctNumber() != null -> text.substring(2).toLong(8).toDouble()
            else -> throw AssertionError() // should never happen
        }
        return NumberLiteral(num)
    }

    override fun visitPrimary(ctx: RolParser.PrimaryContext): Node {
        val text = ctx.text
        return when {
            ctx.Boolean() != null -> BooleanLiteral(text.toBoolean())
            ctx.String() != null -> StringLiteral(text.substring(1, text.length - 1))
            ctx.Null() != null -> NullLiteral
            ctx.identifier() != null -> VariableAccess(text)
            else -> visitChildren(ctx)
        }
    }

    override fun visitCall(ctx: RolParser.CallContext): Node {
        return FunctionCall(ctx.identifier().text, ctx.expression().map(::visit).map { it.asExpr() })
    }

    override fun visitVarDeclaration(ctx: RolParser.VarDeclarationContext): Node {
        val name = ctx.identifier().text
        val def = VarDef(
            name,
            ctx.CONST() != null,
            if (ctx.type() == null) null else Typename.parse(ctx.type().text)
        )
        return if (ctx.expression() == null) {
            def
        } else {
            Statements(def, VarAssign(name, visit(ctx.expression()).asExpr()))
        }
    }

    override fun visitAssignment(ctx: RolParser.AssignmentContext): Node {
        val name = ctx.identifier().text
        return VarAssign(
            name,
            convertToNormalAssignment(
                AssignType.fromSymbol(ctx.assignmentOp().text),
                name,
                visit(ctx.expression()).asExpr()
            )
        )
    }

    override fun visitIfStatement(ctx: RolParser.IfStatementContext?): Node {
        return super.visitIfStatement(ctx)
    }

    override fun aggregateResult(aggregate: Node?, nextResult: Node?): Node {
        return nextResult ?: aggregate ?: NullLiteral
    }
}

private fun convertToNormalAssignment(assign: AssignType, name: String, expr: Expression): Expression {
    return if (assign.operation == null) {
        expr
    } else {
        BinaryExpression(VariableAccess(name), expr, assign.operation)
    }
}