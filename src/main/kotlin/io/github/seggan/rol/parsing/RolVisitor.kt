package io.github.seggan.rol.parsing

import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.antlr.RolParserBaseVisitor
import io.github.seggan.rol.tree.common.AccessModifier
import io.github.seggan.rol.tree.common.Argument
import io.github.seggan.rol.tree.common.FunctionType
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.VoidType
import io.github.seggan.rol.tree.common.location
import io.github.seggan.rol.tree.common.toIdentifier
import io.github.seggan.rol.tree.common.toType
import io.github.seggan.rol.tree.untyped.AssignType
import io.github.seggan.rol.tree.untyped.UAccess
import io.github.seggan.rol.tree.untyped.UBinaryExpression
import io.github.seggan.rol.tree.untyped.UBinaryOperator
import io.github.seggan.rol.tree.untyped.UBooleanLiteral
import io.github.seggan.rol.tree.untyped.UCall
import io.github.seggan.rol.tree.untyped.UClassDef
import io.github.seggan.rol.tree.untyped.UExpression
import io.github.seggan.rol.tree.untyped.UExtern
import io.github.seggan.rol.tree.untyped.UFieldDef
import io.github.seggan.rol.tree.untyped.UIfStatement
import io.github.seggan.rol.tree.untyped.ULambda
import io.github.seggan.rol.tree.untyped.UNode
import io.github.seggan.rol.tree.untyped.UNullLiteral
import io.github.seggan.rol.tree.untyped.UNumberLiteral
import io.github.seggan.rol.tree.untyped.UPostfixExpression
import io.github.seggan.rol.tree.untyped.UPostfixOperator
import io.github.seggan.rol.tree.untyped.UPrefixExpression
import io.github.seggan.rol.tree.untyped.UPrefixOperator
import io.github.seggan.rol.tree.untyped.UReturn
import io.github.seggan.rol.tree.untyped.UStatements
import io.github.seggan.rol.tree.untyped.UStringLiteral
import io.github.seggan.rol.tree.untyped.UVarAssign
import io.github.seggan.rol.tree.untyped.UVarDef
import io.github.seggan.rol.tree.untyped.UVariableAccess

class RolVisitor : RolParserBaseVisitor<UNode>() {

    override fun visitFile(ctx: RolParser.FileContext): UNode {
        return visitStatements(ctx.statements())
    }

    override fun visitStatements(ctx: RolParser.StatementsContext): UStatements {
        return UStatements(ctx.statement().map(::visit).flatMap(::flattenStatements))
    }

    override fun visitExpression(ctx: RolParser.ExpressionContext): UExpression {
        return when {
            ctx.postfixOp != null -> UPostfixExpression(
                visitExpression(ctx.expression(0)),
                UPostfixOperator.fromSymbol(ctx.postfixOp.text),
                ctx.location
            )
            ctx.prefixOp != null -> UPrefixExpression(
                visitExpression(ctx.expression(0)),
                UPrefixOperator.fromSymbol(ctx.prefixOp.text),
                ctx.location
            )
            ctx.op != null -> UBinaryExpression(
                visitExpression(ctx.expression(0)),
                visitExpression(ctx.expression(1)),
                UBinaryOperator.fromSymbol(ctx.op.text),
                ctx.location
            )
            ctx.nonNullAssertion != null -> UPostfixExpression(
                visitExpression(ctx.expression(0)),
                UPostfixOperator.NOT_NULL,
                ctx.location
            )
            ctx.RPAREN() != null -> UCall(
                visitExpression(ctx.expression(0)),
                ctx.args.map(::visitExpression),
                ctx.location
            )
            ctx.primary() != null -> visitPrimary(ctx.primary())
            else -> throw AssertionError() // should never happen
        }
    }

    override fun visitNumber(ctx: RolParser.NumberContext): UNumberLiteral {
        val text = ctx.text.replace("_", "")
        val num = when {
            ctx.Number() != null -> text.toBigDecimal()
            ctx.HexNumber() != null -> text.substring(2).toLong(16).toBigDecimal()
            ctx.BinNumber() != null -> text.substring(2).toLong(2).toBigDecimal()
            ctx.OctNumber() != null -> text.substring(2).toLong(8).toBigDecimal()
            else -> throw AssertionError() // should never happen
        }
        return UNumberLiteral(num, ctx.location)
    }

    override fun visitPrimary(ctx: RolParser.PrimaryContext): UExpression {
        val text = ctx.text
        return when {
            ctx.Boolean() != null -> UBooleanLiteral(text.toBoolean(), ctx.location)
            ctx.string() != null -> UStringLiteral(parseString(ctx.string()), ctx.location)
            ctx.Null() != null -> UNullLiteral(ctx.location)
            ctx.identifier() != null -> UVariableAccess(ctx.identifier().toIdentifier(), ctx.location)
            else -> visitChildren(ctx) as UExpression
        }
    }

    override fun visitExtern(ctx: RolParser.ExternContext): UNode {
        return UExtern(
            ctx.EXTERN_ID().map { it.text },
            ctx.EXTERN_CODE().joinToString("") { it.text },
            ctx.location
        )
    }

    override fun visitVarDeclaration(ctx: RolParser.VarDeclarationContext): UNode {
        val name = ctx.unqualifiedIdentifier().toIdentifier()
        val def = UVarDef(
            name,
            Modifiers(AccessModifier.parse(ctx.accessModifier()), ctx.CONST() != null),
            if (ctx.type() == null) null else ctx.type().toType(),
            ctx.location
        )
        return if (ctx.expression() == null) {
            def
        } else {
            UStatements(def, UVarAssign(name, visitExpression(ctx.expression()), ctx.location))
        }
    }

    override fun visitAssignment(ctx: RolParser.AssignmentContext): UNode {
        val name = ctx.identifier().map { it.toIdentifier() }
        if (name.size == 1) {
            return UVarAssign(
                name[0],
                convertToNormalAssignment(
                    AssignType.fromSymbol(ctx.assignmentOp().text),
                    name,
                    visitExpression(ctx.expression())
                ),
                ctx.location
            )
        }
        TODO("multi-assignment")
    }

    override fun visitIfStatement(ctx: RolParser.IfStatementContext): UNode {
        return UIfStatement(
            visitExpression(ctx.expression()),
            visit(ctx.block(0)).asStatements(),
            if (ctx.block().size > 1) visit(ctx.block(1)).asStatements() else null,
            ctx.location
        )
    }

    // TODO the rest

    override fun visitFunctionDeclaration(ctx: RolParser.FunctionDeclarationContext): UNode {
        val args =
            ctx.argList().arg().map { Argument(it.unqualifiedIdentifier().text, it.type().toType(), it.location) }
        val functionType = FunctionType(args.map(Argument::type), ctx.type()?.toType() ?: VoidType)
        return UStatements(
            UVarDef(
                ctx.identifier().toIdentifier(),
                Modifiers(AccessModifier.parse(ctx.accessModifier()), false),
                functionType,
                ctx.location
            ),
            UVarAssign(
                ctx.identifier().toIdentifier(),
                ULambda(
                    args,
                    visitStatements(ctx.block().statements()),
                    functionType,
                    ctx.location
                ),
                ctx.location
            )
        )
    }

    override fun visitReturnStatement(ctx: RolParser.ReturnStatementContext): UNode {
        return UReturn(
            if (ctx.expression() == null) null else visitExpression(ctx.expression()),
            ctx.location
        )
    }

    override fun visitLambda(ctx: RolParser.LambdaContext): ULambda {
        val args = ctx.arg().map { Argument(it.unqualifiedIdentifier().text, it.type().toType(), it.location) }
        val body = if (ctx.expression() != null) {
            UReturn(visitExpression(ctx.expression()), ctx.location).asStatements()
        } else {
            visitStatements(ctx.statements())
        }
        return ULambda(args, body, ctx.type()?.toType() as FunctionType, ctx.location)
    }

    override fun visitClassDeclaration(ctx: RolParser.ClassDeclarationContext): UNode {
        val funs = ctx.functionDeclaration()
            .map(::visitFunctionDeclaration)
            .map {
                val funDef = it.children[0] as UVarDef
                val funAssign = it.children[1] as UVarAssign
                UFieldDef(funDef.name, funDef.type, funAssign.value, funDef.location)
            }
        return UClassDef(
            ctx.identifier(0).toIdentifier(),
            ctx.fieldDeclaration().map(::visitFieldDeclaration) + funs,
            ctx.supers.map { it.toIdentifier() },
            ctx.location
        )
    }

    override fun visitFieldDeclaration(ctx: RolParser.FieldDeclarationContext): UFieldDef {
        return UFieldDef(
            ctx.identifier().toIdentifier(),
            ctx.type().toType(),
            visitExpression(ctx.expression()),
            ctx.location
        )
    }

    override fun aggregateResult(aggregate: UNode?, nextResult: UNode?): UNode {
        return nextResult ?: aggregate ?: UNullLiteral(Location(0, 0))
    }
}

private fun convertToNormalAssignment(assign: AssignType, name: List<Identifier>, expr: UExpression): UExpression {
    return if (assign.operation == null) {
        expr
    } else {
        val access = if (name.size == 1) {
            UVariableAccess(name[0], expr.location)
        } else {
            var a = UAccess(UVariableAccess(name[0], expr.location), name[1].name, expr.location)
            for (i in 2 until name.size) {
                a = UAccess(a, name[i].name, expr.location)
            }
            a
        }
        UBinaryExpression(access, expr, assign.operation, expr.location)
    }
}

private fun UNode.asStatements(): UStatements {
    return if (this is UStatements) this else UStatements(this)
}

private val replacements = buildMap {
    put("\\\\n".toRegex(), "\n")
    put("\\\\t".toRegex(), "\t")
    put("\\\\r".toRegex(), "\r")
}

fun parseString(input: RolParser.StringContext): String {
    val s = if (input.MultilineString() != null) {
        val text = input.MultilineString().text
        text.substring(3, text.length - 3).trimIndent()
    } else {
        val text = input.String().text
        text.substring(1, text.length - 1)
    }
    return replacements.entries.fold(s) { acc, (regex, replacement) -> regex.replace(acc, replacement) }
}


private fun flattenStatements(node: UNode): List<UNode> =
    if (node is UStatements) node.children.flatMap(::flattenStatements) else listOf(node)