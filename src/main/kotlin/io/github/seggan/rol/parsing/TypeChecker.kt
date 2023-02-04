package io.github.seggan.rol.parsing

import io.github.seggan.rol.Errors
import io.github.seggan.rol.postype.NodeCollector
import io.github.seggan.rol.resolution.DependencyManager
import io.github.seggan.rol.resolution.TypeResolver
import io.github.seggan.rol.tree.common.AccessModifier
import io.github.seggan.rol.tree.common.ConcreteType
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.common.VoidType
import io.github.seggan.rol.tree.typed.TBinaryExpression
import io.github.seggan.rol.tree.typed.TBinaryOperator
import io.github.seggan.rol.tree.typed.TBoolean
import io.github.seggan.rol.tree.typed.TExpression
import io.github.seggan.rol.tree.typed.TFunctionCall
import io.github.seggan.rol.tree.typed.TIfStatement
import io.github.seggan.rol.tree.typed.TLambda
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
import io.github.seggan.rol.tree.untyped.UFunctionCall
import io.github.seggan.rol.tree.untyped.UIfStatement
import io.github.seggan.rol.tree.untyped.ULambda
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

class TypeChecker(
    dependencyManager: DependencyManager,
    imports: Set<String>,
    private val pkg: String
) {

    private val stack = ArrayDeque<StackFrame>()
    private val currentFrame get() = stack.first()
    private val resolver = TypeResolver(dependencyManager, pkg, imports)

    fun typeAst(ast: UStatements): TStatements {
        return typeStatements(ast)
    }

    private fun type(node: UNode): TNode {
        return when (node) {
            is UStatements -> typeStatements(node)
            is UExpression -> typeExpression(node)
            is UVarDef -> typeVariableDeclaration(node).also { currentFrame.vars.add(it) }
            is UVarAssign -> typeVariableAssignment(node)
            is UReturn -> typeReturn(node)
            is UIfStatement -> typeIfStatement(node)
            else -> throw IllegalArgumentException("Unknown node type: ${node.javaClass}")
        }
    }

    private fun typeIfStatement(node: UIfStatement): TIfStatement {
        val condition = typeExpression(node.cond)
        if (!ConcreteType.BOOLEAN.isAssignableFrom(condition.type)) {
            Errors.typeMismatch(ConcreteType.BOOLEAN, condition.type, condition.location)
        }
        val body = typeStatements(node.ifTrue)
        val elseBody = node.ifFalse?.let { typeStatements(it) }
        return TIfStatement(condition, body, elseBody, node.location)
    }

    private fun typeLambda(node: ULambda): TLambda {
        val args = node.args.map { it.copy(type = resolver.resolveType(it.type, it.location)) }
        val body = typeStatements(node.body, args.map {
            TVarDef(
                it.name, it.type, Modifiers(
                    AccessModifier.PRIVATE,
                    const = true
                ), it.location
            )
        })
        val returns = object : NodeCollector<TReturn>() {
            override fun visitReturn(ret: TReturn) = add(ret)
        }.collect(body)
        val returnType = returns.map(TNode::type).reduce { a, b ->
            if (a.isAssignableFrom(b)) b else if (b.isAssignableFrom(a)) a else Errors.typeMismatch(a, b, node.location)
        }
        return TLambda(args, body, returnType, node.location)
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
            is ULambda -> typeLambda(expr)
            else -> throw IllegalArgumentException("Unknown expression type: ${expr.javaClass}")
        }
    }

    private fun typeVariableAccess(expr: UVariableAccess): TVariableAccess {
        val varDef = currentFrame.vars.find { it.name == expr.name }
            ?: Errors.undefinedReference(expr.name, expr.location)
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
                if (ConcreteType.STRING.isAssignableFrom(left.type)) {
                    if (ConcreteType.STRING.isAssignableFrom(right.type)) {
                        return TBinaryExpression(left, right, TBinaryOperator.CONCAT, expr.location)
                    } else {
                        Errors.typeMismatch(ConcreteType.STRING, right.type, right.location)
                    }
                } else if (ConcreteType.NUMBER.isAssignableFrom(left.type)) {
                    if (ConcreteType.NUMBER.isAssignableFrom(right.type)) {
                        return TBinaryExpression(left, right, TBinaryOperator.PLUS, expr.location)
                    } else {
                        Errors.typeMismatch(ConcreteType.NUMBER, right.type, right.location)
                    }
                } else {
                    Errors.typeMismatch(ConcreteType.STRING, left.type, left.location)
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
            if (ConcreteType.NUMBER.isAssignableFrom(operand.type)) {
                return TBinaryExpression(TNumber(-1, expr.location), operand, TBinaryOperator.MINUS, expr.location)
            } else {
                Errors.typeMismatch(ConcreteType.NUMBER, operand.type, operand.location)
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
            return TVarDef(name, resolver.resolveType(type, decl.location), decl.modifiers, decl.location)
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
        val name = call.fname
        val args = call.args.map { typeExpression(it) }
        val returnType = resolver.findFunction(name.pkg, name.name, args.map(TNode::type), call.location)
        if (returnType == null) {
            val errorName = name.toString() + args.joinToString(", ", "(", ")") { it.type.toString() }
            Errors.undefinedReference(errorName, call.location)
        } else {
            return TFunctionCall(Identifier(name.name, returnType.first), args, returnType.second, call.location)
        }
    }

    private fun typeStatements(
        statements: UStatements,
        extraVars: List<TVarDef> = emptyList(),
        returnType: Type = if (stack.isEmpty()) VoidType else currentFrame.returnType
    ): TStatements {
        val vars = if (stack.isEmpty()) mutableListOf() else currentFrame.vars
        vars.addAll(extraVars)
        stack.addFirst(StackFrame(vars, statements, returnType))
        return TStatements(statements.children.map(::type), statements.location).also { stack.removeFirst() }
    }
}

private fun checkVoid(expr: TExpression): TExpression {
    if (expr is TFunctionCall && expr.type == VoidType) {
        Errors.genericError(
            "Type inference",
            "The non-returning function ${expr.field} cannot be used in an expression",
            expr.location
        )
    }
    return expr
}

private data class StackFrame(val vars: MutableList<TVarDef>, val statements: UStatements, val returnType: Type)

