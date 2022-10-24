package io.github.seggan.rol.parsing

import io.github.seggan.rol.DependencyManager
import io.github.seggan.rol.Errors
import io.github.seggan.rol.tree.common.AccessModifier
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.typed.TBinaryExpression
import io.github.seggan.rol.tree.typed.TBinaryOperator
import io.github.seggan.rol.tree.typed.TBoolean
import io.github.seggan.rol.tree.typed.TExpression
import io.github.seggan.rol.tree.typed.TExternDeclaration
import io.github.seggan.rol.tree.typed.TFn
import io.github.seggan.rol.tree.typed.TFunctionCall
import io.github.seggan.rol.tree.typed.TFunctionDeclaration
import io.github.seggan.rol.tree.typed.TIfStatement
import io.github.seggan.rol.tree.typed.TLiteral
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.typed.TNull
import io.github.seggan.rol.tree.typed.TNumber
import io.github.seggan.rol.tree.typed.TPostfixExpression
import io.github.seggan.rol.tree.typed.TPrefixExpression
import io.github.seggan.rol.tree.typed.TReturn
import io.github.seggan.rol.tree.typed.TStatements
import io.github.seggan.rol.tree.typed.TString
import io.github.seggan.rol.tree.typed.TVarAssign
import io.github.seggan.rol.tree.typed.TVarDef
import io.github.seggan.rol.tree.typed.TVariableAccess
import io.github.seggan.rol.tree.untyped.UBinaryExpression
import io.github.seggan.rol.tree.untyped.UBinaryOperator
import io.github.seggan.rol.tree.untyped.UBooleanLiteral
import io.github.seggan.rol.tree.untyped.UExpression
import io.github.seggan.rol.tree.untyped.UExternDeclaration
import io.github.seggan.rol.tree.untyped.UFunctionCall
import io.github.seggan.rol.tree.untyped.UFunctionDeclaration
import io.github.seggan.rol.tree.untyped.UIfStatement
import io.github.seggan.rol.tree.untyped.ULiteral
import io.github.seggan.rol.tree.untyped.UNode
import io.github.seggan.rol.tree.untyped.UNullLiteral
import io.github.seggan.rol.tree.untyped.UNumberLiteral
import io.github.seggan.rol.tree.untyped.UPostfixExpression
import io.github.seggan.rol.tree.untyped.UPrefixExpression
import io.github.seggan.rol.tree.untyped.UReturn
import io.github.seggan.rol.tree.untyped.UStatements
import io.github.seggan.rol.tree.untyped.UStringLiteral
import io.github.seggan.rol.tree.untyped.UVarAssign
import io.github.seggan.rol.tree.untyped.UVarDef
import io.github.seggan.rol.tree.untyped.UVariableAccess

class TypeChecker(private val dependencyManager: DependencyManager, private val imports: Set<String>) {

    private val stack = ArrayDeque<StackFrame>()
    private val currentFrame get() = stack.first()
    private val typedFunctions = mutableSetOf<TFn>()

    fun typeAst(ast: UStatements): TStatements {
        return typeStatements(ast)
    }

    private fun type(node: UNode): TNode {
        return when (node) {
            is UStatements -> typeStatements(node)
            is UExpression -> typeExpression(node)
            is UVarDef -> typeVariableDeclaration(node).also { currentFrame.vars.add(it) }
            is UVarAssign -> typeVariableAssignment(node)
            is UExternDeclaration -> typeExternDeclaration(node)
            is UFunctionDeclaration -> typeFunctionDeclaration(node)
            is UReturn -> typeReturn(node)
            is UIfStatement -> typeIfStatement(node)
            else -> throw IllegalArgumentException("Unknown node type: ${node.javaClass}")
        }
    }

    private fun typeIfStatement(node: UIfStatement): TIfStatement {
        val condition = typeExpression(node.cond)
        if (!Type.BOOLEAN.isAssignableFrom(condition.type)) {
            Errors.typeMismatch(Type.BOOLEAN, condition.type, condition.location)
        }
        val body = typeStatements(node.ifTrue)
        val elseBody = node.ifFalse?.let { typeStatements(it) }
        return TIfStatement(condition, body, elseBody, node.location)
    }

    private fun typeFunctionDeclaration(node: UFunctionDeclaration): TFunctionDeclaration {
        val args = node.args
        val type = node.type
        return TFunctionDeclaration(
            node.name,
            args,
            type,
            node.modifiers,
            typeStatements(node.body, args.map { TVarDef(it.name, it.type, Modifiers(
                AccessModifier.PRIVATE,
                const = true
            ), it.location) }, type),
            node.location
        )
    }

    private fun typeExternDeclaration(declaration: UExternDeclaration): TExternDeclaration {
        return TExternDeclaration(
            declaration.name,
            declaration.nativeName,
            declaration.args,
            declaration.location
        )
    }

    private fun typeReturn(node: UReturn): TNode {
        val statement = TReturn(if (node.value == null) null else typeExpression(node.value), node.location)
        if (!currentFrame.returnType.isAssignableFrom(statement.type)) {
            Errors.typeMismatch(currentFrame.returnType, statement.type, statement.location)
        }
        return statement
    }

    private fun typeExpression(expr: UExpression): TExpression {
        return when (expr) {
            is UBinaryExpression -> typeBinaryExpression(expr)
            is UPrefixExpression -> typePrefixExpression(expr)
            is UPostfixExpression -> typePostfixExpression(expr)
            is ULiteral -> typeLiteral(expr)
            is UFunctionCall -> typeFunctionCall(expr)
            is UVariableAccess -> typeVariableAccess(expr)
            else -> throw IllegalArgumentException("Unknown expression type: ${expr.javaClass}")
        }
    }

    private fun typeVariableAccess(expr: UVariableAccess): TVariableAccess {
        val varDef = currentFrame.vars.find { it.name == expr.name } ?: Errors.undefinedReference(expr.name, expr.location)
        return TVariableAccess(varDef.name, varDef.type, expr.location)
    }

    private fun typeLiteral(literal: ULiteral): TLiteral<*> {
        return when (literal) {
            is UNumberLiteral -> TNumber(literal.value, literal.location)
            is UStringLiteral -> TString(literal.value, literal.location)
            is UBooleanLiteral -> TBoolean(literal.value, literal.location)
            is UNullLiteral -> TNull(literal.location)
        }
    }

    private fun typeBinaryExpression(expr: UBinaryExpression): TBinaryExpression {
        val left = checkVoid(typeExpression(expr.left))
        val right = checkVoid(typeExpression(expr.right))
        val op = expr.type.typedOperator
        if (op != null) {
            val argType = op.argType
            if (argType.isAssignableFrom(left.type)) {
                if (argType.isAssignableFrom(right.type)) {
                    return TBinaryExpression(left, right, op, expr.location)
                } else {
                    Errors.typeMismatch(argType, right.type, right.location)
                }
            } else {
                Errors.typeMismatch(argType, left.type, left.location)
            }
        } else {
            // special handling for string concat
            if (expr.type == UBinaryOperator.PLUS) {
                if (Type.STRING.isAssignableFrom(left.type)) {
                    if (Type.STRING.isAssignableFrom(right.type)) {
                        return TBinaryExpression(left, right, TBinaryOperator.CONCAT, expr.location)
                    } else {
                        Errors.typeMismatch(Type.STRING, right.type, right.location)
                    }
                } else if (Type.NUMBER.isAssignableFrom(left.type)) {
                    if (Type.NUMBER.isAssignableFrom(right.type)) {
                        return TBinaryExpression(left, right, TBinaryOperator.PLUS, expr.location)
                    } else {
                        Errors.typeMismatch(Type.NUMBER, right.type, right.location)
                    }
                } else {
                    Errors.typeMismatch(Type.STRING, left.type, left.location)
                }
            } else {
                throw AssertionError() // should never happen
            }
        }
    }

    private fun typePrefixExpression(expr: UPrefixExpression): TExpression {
        val operand = checkVoid(typeExpression(expr.expr))
        val op = expr.type.typedOperator
        if (op == null) {
            // can be replaced with a subtraction
            if (Type.NUMBER.isAssignableFrom(operand.type)) {
                return TBinaryExpression(TNumber(-1, expr.location), operand, TBinaryOperator.MINUS, expr.location)
            } else {
                Errors.typeMismatch(Type.NUMBER, operand.type, operand.location)
            }
        } else {
            if (op.argType.isAssignableFrom(operand.type)) {
                return TPrefixExpression(operand, op, expr.location)
            } else {
                Errors.typeMismatch(op.argType, operand.type, operand.location)
            }
        }
    }

    private fun typePostfixExpression(expr: UPostfixExpression): TExpression {
        val operand = checkVoid(typeExpression(expr.expr))
        val op = expr.type.typedOperator
        val result = op.typer(operand.type)
        if (result == null) {
            Errors.typeMismatch(op.expect, operand.type, operand.location)
        } else {
            return TPostfixExpression(operand, op, result, expr.location)
        }
    }

    private fun typeVariableDeclaration(decl: UVarDef): TVarDef {
        val type = decl.type
        val name = decl.name
        if (type == null) {
            // we have to infer the type
            val first = currentFrame.statements
                .shallowFlatten()
                .filterIsInstance<UVarAssign>()
                .firstOrNull { it.name == name }
            if (first != null) {
                val expr = typeExpression(first.value)
                return TVarDef(name, expr.type, decl.modifiers, decl.location)
            } else {
                Errors.genericError(
                    "Type inference",
                    "Cannot infer type of variable $name",
                    decl.location
                )
            }
        } else {
            return TVarDef(name, type, decl.modifiers, decl.location)
        }
    }

    private fun typeVariableAssignment(node: UVarAssign): TVarAssign {
        val name = node.name
        val value = typeExpression(node.value)
        val type = currentFrame.vars.find { it.name == name }?.type ?: Errors.undefinedReference(name, node.location)
        if (type.isAssignableFrom(value.type)) {
            return TVarAssign(name, type, value, node.location)
        } else {
            Errors.typeMismatch(type, value.type, value.location)
        }
    }

    private fun typeFunctionCall(call: UFunctionCall): TFunctionCall {
        val name = call.name
        val args = call.args.map { typeExpression(it) }
        funcLoop@ for (func in typedFunctions.filter { it.name == name }) {
            if (func.args.size == args.size) {
                for (i in args.indices) {
                    if (!func.args[i].type.isAssignableFrom(args[i].type)) {
                        continue@funcLoop
                    }
                }
                return TFunctionCall(name, args, func.type, call.location)
            }
        }
        for (import in imports) {
            for (dep in dependencyManager.getPackage(import)) {
                val func = dep.findFunction(name, args.map(TNode::type))
                if (func != null) {
                    dependencyManager.usedDependencies.add(dep)
                    return TFunctionCall(name, args, func.returnType, call.location)
                }
            }
        }
        val errorName = name + args.joinToString(", ", "(", ")") { it.type.name }
        Errors.undefinedReference(errorName, call.location)
    }

    private fun typeStatements(
        statements: UStatements,
        extraVars: List<TVarDef> = emptyList(),
        returnType: Type = if (stack.isEmpty()) Type.VOID else currentFrame.returnType
    ): TStatements {
        val flat = statements.shallowFlatten()
        typedFunctions.addAll(flat.filterIsInstance<UExternDeclaration>().map(::typeExternDeclaration))
        typedFunctions.addAll(flat.filterIsInstance<UFunctionDeclaration>().map { typeFunctionDeclaration(it) })
        val vars = if (stack.isEmpty()) mutableListOf() else currentFrame.vars
        vars.addAll(extraVars)
        stack.addFirst(StackFrame(vars, statements, returnType))
        return TStatements(statements.children.map(::type), statements.location).also { stack.removeFirst() }
    }
}

private fun checkVoid(expr: TExpression): TExpression {
    if (expr is TFunctionCall && expr.type == Type.VOID) {
        Errors.genericError(
            "Type inference",
            "The non-returning function ${expr.name} cannot be used in an expression",
            expr.location
        )
    }
    return expr
}

private data class StackFrame(val vars: MutableList<TVarDef>, val statements: UStatements, val returnType: Type)