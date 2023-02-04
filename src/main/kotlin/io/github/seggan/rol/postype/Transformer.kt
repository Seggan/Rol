package io.github.seggan.rol.postype

import io.github.seggan.rol.tree.typed.TAccess
import io.github.seggan.rol.tree.typed.TBinaryExpression
import io.github.seggan.rol.tree.typed.TExpression
import io.github.seggan.rol.tree.typed.TFunctionCall
import io.github.seggan.rol.tree.typed.TIfStatement
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.typed.TPostfixExpression
import io.github.seggan.rol.tree.typed.TPrefixExpression
import io.github.seggan.rol.tree.typed.TReturn
import io.github.seggan.rol.tree.typed.TStatements
import io.github.seggan.rol.tree.typed.TVarAssign
import io.github.seggan.rol.tree.typed.TypedTreeVisitor

abstract class Transformer : TypedTreeVisitor<TNode>() {

    override fun defaultValue(node: TNode): TNode {
        return node
    }

    override fun visitStatements(statements: TStatements): TNode {
        return TStatements(statements.children.map(::visit), statements.location)
    }

    override fun visitBinaryExpression(expression: TBinaryExpression): TNode {
        return TBinaryExpression(
            visit(expression.left) as TExpression,
            visit(expression.right) as TExpression,
            expression.operator,
            expression.location
        )
    }

    override fun visitPrefixExpression(expression: TPrefixExpression): TNode {
        return TPrefixExpression(
            visit(expression.right) as TExpression,
            expression.operator,
            expression.location
        )
    }

    override fun visitPostfixExpression(expression: TPostfixExpression): TNode {
        return TPostfixExpression(
            visit(expression.left) as TExpression,
            expression.operator,
            expression.type,
            expression.location
        )
    }

    override fun visitFunctionCall(call: TFunctionCall): TNode {
        return TFunctionCall(
            call.fname,
            call.args.map(::visit).filterIsInstance<TExpression>(),
            call.type,
            call.location
        )
    }

    override fun visitVariableAssignment(assignment: TVarAssign): TNode {
        return TVarAssign(
            assignment.name,
            assignment.type,
            visit(assignment.value) as TExpression,
            assignment.location
        )
    }

    override fun visitReturn(ret: TReturn): TNode {
        return TReturn(if (ret.value == null) null else visit(ret.value) as TExpression, ret.location)
    }

    override fun visitIfStatement(statement: TIfStatement): TNode {
        return TIfStatement(
            visit(statement.condition) as TExpression,
            visit(statement.ifBody) as TStatements,
            if (statement.elseBody == null) null else visit(statement.elseBody) as TStatements,
            statement.location
        )
    }

    override fun visitAccess(access: TAccess): TNode {
        return TAccess(
            visit(access.target) as TExpression,
            access.field,
            access.type,
            access.location
        )
    }
}