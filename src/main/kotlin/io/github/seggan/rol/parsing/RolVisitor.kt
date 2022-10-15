package io.github.seggan.rol.parsing

import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.antlr.RolParserBaseVisitor
import io.github.seggan.rol.tree.AccessModifier
import io.github.seggan.rol.tree.Location
import io.github.seggan.rol.tree.location
import io.github.seggan.rol.tree.untyped.AssignType
import io.github.seggan.rol.tree.untyped.UAccess
import io.github.seggan.rol.tree.untyped.UArgument
import io.github.seggan.rol.tree.untyped.UBinaryExpression
import io.github.seggan.rol.tree.untyped.UBinaryOperator
import io.github.seggan.rol.tree.untyped.UBooleanLiteral
import io.github.seggan.rol.tree.untyped.UExpression
import io.github.seggan.rol.tree.untyped.UExternDeclaration
import io.github.seggan.rol.tree.untyped.UFunctionCall
import io.github.seggan.rol.tree.untyped.UFunctionDeclaration
import io.github.seggan.rol.tree.untyped.UIfStatement
import io.github.seggan.rol.tree.untyped.UNode
import io.github.seggan.rol.tree.untyped.UNullLiteral
import io.github.seggan.rol.tree.untyped.UNumberLiteral
import io.github.seggan.rol.tree.untyped.UPostfixExpression
import io.github.seggan.rol.tree.untyped.UPostfixOperator
import io.github.seggan.rol.tree.untyped.UPrefixExpression
import io.github.seggan.rol.tree.untyped.UPrefixOperator
import io.github.seggan.rol.tree.untyped.UStatements
import io.github.seggan.rol.tree.untyped.UStringLiteral
import io.github.seggan.rol.tree.untyped.UTypename
import io.github.seggan.rol.tree.untyped.UVarAssign
import io.github.seggan.rol.tree.untyped.UVarDef
import io.github.seggan.rol.tree.untyped.UVariableAccess
import io.github.seggan.rol.tree.untyped.asExpr

class RolVisitor : RolParserBaseVisitor<UNode>() {

    override fun visitFile(ctx: RolParser.FileContext): UNode {
        fun flattenStatements(node: UNode): List<UNode> =
            if (node is UStatements) node.children.flatMap(::flattenStatements) else listOf(node)

        return UStatements(visitChildren(ctx).children.flatMap(::flattenStatements))
    }

    override fun visitStatements(ctx: RolParser.StatementsContext): UNode {
        return UStatements(ctx.statement().map(::visit))
    }

    override fun visitExpression(ctx: RolParser.ExpressionContext): UNode {
        return when {
            ctx.postfixOp != null -> UPostfixExpression(
                visit(ctx.expression(0)).asExpr(),
                UPostfixOperator.fromSymbol(ctx.postfixOp.text),
                ctx.location
            )
            ctx.prefixOp != null -> UPrefixExpression(
                visit(ctx.expression(0)).asExpr(),
                UPrefixOperator.fromSymbol(ctx.prefixOp.text),
                ctx.location
            )
            ctx.op != null -> UBinaryExpression(
                visit(ctx.expression(0)).asExpr(),
                visit(ctx.expression(1)).asExpr(),
                UBinaryOperator.fromSymbol(ctx.op.text),
                ctx.location
            )
            ctx.nonNullAssertion != null -> UPostfixExpression(
                visit(ctx.expression(0)).asExpr(),
                UPostfixOperator.NOT_NULL,
                ctx.location
            )
            ctx.identifier() != null -> UAccess(visit(ctx.expression(0)).asExpr(), ctx.identifier().text, ctx.location)
            ctx.call() != null -> {
                val call = visit(ctx.call()) as UFunctionCall
                return UFunctionCall(call.name, listOf(visit(ctx.expression(0)).asExpr()) + call.args, call.location)
            }
            else -> visitChildren(ctx)
        }
    }

    override fun visitNumber(ctx: RolParser.NumberContext): UNode {
        val text = ctx.text
        val num = when {
            ctx.Number() != null -> text.toDouble()
            ctx.HexNumber() != null -> text.substring(2).toLong(16).toDouble()
            ctx.BinNumber() != null -> text.substring(2).toLong(2).toDouble()
            ctx.OctNumber() != null -> text.substring(2).toLong(8).toDouble()
            else -> throw AssertionError() // should never happen
        }
        return UNumberLiteral(num, ctx.location)
    }

    override fun visitPrimary(ctx: RolParser.PrimaryContext): UNode {
        val text = ctx.text
        return when {
            ctx.Boolean() != null -> UBooleanLiteral(text.toBoolean(), ctx.location)
            ctx.String() != null -> UStringLiteral(text.substring(1, text.length - 1), ctx.location)
            ctx.Null() != null -> UNullLiteral(ctx.location)
            ctx.identifier() != null -> UVariableAccess(text, ctx.location)
            else -> visitChildren(ctx)
        }
    }

    override fun visitCall(ctx: RolParser.CallContext): UNode {
        return UFunctionCall(ctx.identifier().text, ctx.expression().map(::visit).map { it.asExpr() }, ctx.location)
    }

    override fun visitVarDeclaration(ctx: RolParser.VarDeclarationContext): UNode {
        val name = ctx.identifier().text
        val def = UVarDef(
            name,
            ctx.CONST() != null,
            if (ctx.type() == null) null else UTypename.parse(ctx.type()),
            AccessModifier.parse(ctx.accessModifier()),
            ctx.location
        )
        return if (ctx.expression() == null) {
            def
        } else {
            UStatements(def, UVarAssign(name, visit(ctx.expression()).asExpr(), ctx.location))
        }
    }

    override fun visitAssignment(ctx: RolParser.AssignmentContext): UNode {
        val name = ctx.identifier().text
        return UVarAssign(
            name,
            convertToNormalAssignment(
                AssignType.fromSymbol(ctx.assignmentOp().text),
                name,
                visit(ctx.expression()).asExpr()
            ),
            ctx.location
        )
    }

    override fun visitIfStatement(ctx: RolParser.IfStatementContext): UNode {
        return UIfStatement(
            visit(ctx.expression()).asExpr(),
            visit(ctx.block(0)).asStatements(),
            if (ctx.block().size > 1) visit(ctx.block(1)).asStatements() else null,
            ctx.location
        )
    }

    // TODO the rest

    override fun visitFunctionDeclaration(ctx: RolParser.FunctionDeclarationContext): UNode {
        return UFunctionDeclaration(
            ctx.identifier().text,
            ctx.argList().arg().map { UArgument(it.identifier().text, UTypename.parse(it.type())!!, it.location) },
            AccessModifier.parse(ctx.accessModifier()),
            visit(ctx.block()).asStatements(),
            UTypename.parse(ctx.type()),
            ctx.location
        )
    }

    override fun visitExternDeclaration(ctx: RolParser.ExternDeclarationContext): UNode {
        val name = ctx.identifier().dropLast(if (ctx.name == null) 0 else 1)
            .joinToString(".", transform = RolParser.IdentifierContext::getText)
        return UExternDeclaration(
            if (ctx.name == null) name else ctx.name.text,
            name,
            ctx.noTypeArgList().identifier().map {
                UArgument(
                    it.text,
                    UTypename("dyn", false, it.location),
                    it.location
                )
            },
            ctx.location
        )
    }

    override fun aggregateResult(aggregate: UNode?, nextResult: UNode?): UNode {
        return nextResult ?: aggregate ?: UNullLiteral(Location(0, 0))
    }
}

private fun convertToNormalAssignment(assign: AssignType, name: String, expr: UExpression): UExpression {
    return if (assign.operation == null) {
        expr
    } else {
        UBinaryExpression(UVariableAccess(name, expr.location.copy(text = name)), expr, assign.operation, expr.location)
    }
}

private fun UNode.asStatements(): UStatements {
    return if (this is UStatements) this else UStatements(this)
}