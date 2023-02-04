package io.github.seggan.rol.tree.typed

open class TypedTreeVisitor<R> {

    open fun start(node: TNode): R {
        return visit(node)
    }

    protected fun visit(node: TNode): R {
        return node.accept(this)
    }

    protected open fun defaultValue(node: TNode): R {
        throw UnsupportedOperationException("Unsupported node type: ${node::class.simpleName}")
    }

    open fun visitStatements(statements: TStatements): R {
        return statements.children.map(::visit).last()
    }

    open fun visitBinaryExpression(expression: TBinaryExpression): R {
        expression.children.forEach(::visit)
        return defaultValue(expression)
    }

    open fun visitPrefixExpression(expression: TPrefixExpression): R {
        expression.children.forEach(::visit)
        return defaultValue(expression)
    }

    open fun visitPostfixExpression(expression: TPostfixExpression): R {
        expression.children.forEach(::visit)
        return defaultValue(expression)
    }

    open fun visitNumber(num: TNumber): R {
        return defaultValue(num)
    }

    open fun visitString(string: TString): R {
        return defaultValue(string)
    }

    open fun visitBoolean(bool: TBoolean): R {
        return defaultValue(bool)
    }

    open fun visitNull(tNull: TNull): R {
        return defaultValue(tNull)
    }

    open fun visitFunctionCall(call: TFunctionCall): R {
        call.args.forEach(::visit)
        return defaultValue(call)
    }

    open fun visitVariableDeclaration(declaration: TVarDef): R {
        return defaultValue(declaration)
    }

    open fun visitVariableAccess(access: TVariableAccess): R {
        return defaultValue(access)
    }

    open fun visitVariableAssignment(assignment: TVarAssign): R {
        visit(assignment.value)
        return defaultValue(assignment)
    }

    open fun visitReturn(ret: TReturn): R {
        if (ret.value != null) {
            visit(ret.value)
        }
        return defaultValue(ret)
    }

    open fun visitIfStatement(statement: TIfStatement): R {
        visit(statement.condition)
        visit(statement.ifBody)
        if (statement.elseBody != null) {
            visit(statement.elseBody)
        }
        return defaultValue(statement)
    }

    open fun visitAccess(access: TAccess): R {
        visit(access.target)
        return defaultValue(access)
    }

    fun visitLambda(lambda: TLambda): R {
        visit(lambda.body)
        return defaultValue(lambda)
    }
}