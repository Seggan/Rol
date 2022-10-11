package io.github.seggan.rol.parsing

import io.github.seggan.rol.DependencyManager
import io.github.seggan.rol.Errors
import io.github.seggan.rol.tree.typed.TArgument
import io.github.seggan.rol.tree.typed.TBinaryExpression
import io.github.seggan.rol.tree.typed.TBinaryOperator
import io.github.seggan.rol.tree.typed.TBoolean
import io.github.seggan.rol.tree.typed.TExpression
import io.github.seggan.rol.tree.typed.TExternDeclaration
import io.github.seggan.rol.tree.typed.TFn
import io.github.seggan.rol.tree.typed.TFunctionCall
import io.github.seggan.rol.tree.typed.TFunctionDeclaration
import io.github.seggan.rol.tree.typed.TLiteral
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.typed.TNull
import io.github.seggan.rol.tree.typed.TNumber
import io.github.seggan.rol.tree.typed.TPrefixExpression
import io.github.seggan.rol.tree.typed.TStatements
import io.github.seggan.rol.tree.typed.TString
import io.github.seggan.rol.tree.typed.TVarAssign
import io.github.seggan.rol.tree.typed.TVarDef
import io.github.seggan.rol.tree.typed.TVariableAccess
import io.github.seggan.rol.tree.typed.Type
import io.github.seggan.rol.tree.untyped.UBinaryExpression
import io.github.seggan.rol.tree.untyped.UBinaryOperator
import io.github.seggan.rol.tree.untyped.UBooleanLiteral
import io.github.seggan.rol.tree.untyped.UExpression
import io.github.seggan.rol.tree.untyped.UExternDeclaration
import io.github.seggan.rol.tree.untyped.UFunctionCall
import io.github.seggan.rol.tree.untyped.UFunctionDeclaration
import io.github.seggan.rol.tree.untyped.ULiteral
import io.github.seggan.rol.tree.untyped.UNode
import io.github.seggan.rol.tree.untyped.UNullLiteral
import io.github.seggan.rol.tree.untyped.UNumberLiteral
import io.github.seggan.rol.tree.untyped.UPrefixExpression
import io.github.seggan.rol.tree.untyped.UStatements
import io.github.seggan.rol.tree.untyped.UStringLiteral
import io.github.seggan.rol.tree.untyped.UVarAssign
import io.github.seggan.rol.tree.untyped.UVarDef
import io.github.seggan.rol.tree.untyped.UVariableAccess
import kotlin.math.pow

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
            else -> throw IllegalArgumentException("Unknown node type: ${node.javaClass}")
        }
    }

    private fun typeFunctionDeclaration(node: UFunctionDeclaration): TFunctionDeclaration {
        val args = node.args.map { TArgument(it.name, it.type.toType(), it.location) }
        return TFunctionDeclaration(
            node.name,
            args,
            node.type?.toType() ?: Type.VOID,
            node.access,
            typeStatements(node.body),
            node.location
        )
    }

    private fun typeExternDeclaration(declaration: UExternDeclaration): TExternDeclaration {
        return TExternDeclaration(
            declaration.name,
            declaration.nativeName,
            declaration.args.map { TArgument(it.name, Type.ANY, it.location) },
            declaration.location
        )
    }

    private fun typeExpression(expr: UExpression): TExpression {
        return when (expr) {
            is UBinaryExpression -> typeBinaryExpression(expr)
            is UPrefixExpression -> typePrefixExpression(expr)
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

    private fun typeLiteral(literal: ULiteral): TLiteral {
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
            // special handling for string concat and shifts
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
            } else if (expr.type == UBinaryOperator.SHIFT_LEFT) {
                // can be replaced with a multiplication in the case of literals
                if (Type.NUMBER.isAssignableFrom(left.type)) {
                    if (Type.NUMBER.isAssignableFrom(right.type)) {
                        return if (right is TNumber) {
                            TBinaryExpression(
                                left, TNumber(2.0.pow(right.value), right.location),
                                TBinaryOperator.MULTIPLY, expr.location
                            )
                        } else {
                            TBinaryExpression(left, right, TBinaryOperator.BITWISE_SHIFT_LEFT, expr.location)
                        }
                    } else {
                        Errors.typeMismatch(Type.NUMBER, right.type, right.location)
                    }
                } else {
                    Errors.typeMismatch(Type.NUMBER, left.type, left.location)
                }
            } else if (expr.type == UBinaryOperator.SHIFT_RIGHT) {
                // can be replaced with a division in the case of literals
                if (Type.NUMBER.isAssignableFrom(left.type)) {
                    if (Type.NUMBER.isAssignableFrom(right.type)) {
                        return if (right is TNumber) {
                            TBinaryExpression(
                                left, TNumber(2.0.pow(right.value), right.location),
                                TBinaryOperator.DIVIDE, expr.location
                            )
                        } else {
                            TBinaryExpression(left, right, TBinaryOperator.BITWISE_SHIFT_RIGHT, expr.location)
                        }
                    } else {
                        Errors.typeMismatch(Type.NUMBER, right.type, right.location)
                    }
                } else {
                    Errors.typeMismatch(Type.NUMBER, left.type, left.location)
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

    private fun typeVariableDeclaration(decl: UVarDef): TVarDef {
        val typename = decl.type
        val name = decl.name
        if (typename == null) {
            // we have to infer the type
            val first = currentFrame.statements
                .shallowFlatten()
                .filterIsInstance<UVarAssign>()
                .firstOrNull { it.name == name }
            if (first != null) {
                val expr = typeExpression(first.value)
                return TVarDef(name, expr.type, decl.access, decl.location)
            } else {
                Errors.genericError(
                    "Type inference",
                    "Cannot infer type of variable $name",
                    decl.location
                )
            }
        } else {
            return TVarDef(name, typename.toType(), decl.access, decl.location)
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
            val dep = dependencyManager.getDependency(import) ?: continue
            for (func in dep.functions.filter { it.name == name }) {
                if (func.matches(call, args)) {
                    return TFunctionCall(name, args, func.returnType, call.location)
                }
            }
        }
        Errors.undefinedReference(name, call.location)
    }

    private fun typeStatements(statements: UStatements): TStatements {
        val flat = statements.shallowFlatten()
        typedFunctions.addAll(flat.filterIsInstance<UFunctionDeclaration>().map(::typeFunctionDeclaration))
        typedFunctions.addAll(flat.filterIsInstance<UExternDeclaration>().map(::typeExternDeclaration))
        stack.addFirst(StackFrame(currentFrame.vars, statements))
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

private data class StackFrame(val vars: MutableList<TVarDef>, val statements: UStatements)