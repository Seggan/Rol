package io.github.seggan.rol.tree.typed

open class TypedTreeVisitor<R> {

    fun visit(node: TNode): R {
        return node.accept(this)
    }

    protected open fun defaultValue(node: TNode): R {
        throw UnsupportedOperationException("Unsupported node type: ${node::class.simpleName}")
    }

    open fun visitStatements(statements: TStatements): R {
        return statements.children.map(::visit).last()
    }

    fun visitBinaryExpression(expression: TBinaryExpression): R {
        expression.children.forEach(::visit)
        return defaultValue(expression)
    }

    open fun visitPrefixExpression(expression: TPrefixExpression): R {
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

    open fun visitFunctionDeclaration(declaration: TFunctionDeclaration): R {
        return defaultValue(declaration)
    }

    open fun visitExternDeclaration(declaration: TExternDeclaration): R {
        return defaultValue(declaration)
    }

    open fun visitArgument(arg: TArgument): R {
        return defaultValue(arg)
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
}