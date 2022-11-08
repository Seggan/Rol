package io.github.seggan.rol.parsing

import io.github.seggan.rol.DependencyManager
import io.github.seggan.rol.Errors
import io.github.seggan.rol.tree.common.AccessModifier
import io.github.seggan.rol.tree.common.Argument
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Struct
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.typed.TAccess
import io.github.seggan.rol.tree.typed.TBinaryExpression
import io.github.seggan.rol.tree.typed.TBinaryOperator
import io.github.seggan.rol.tree.typed.TBoolean
import io.github.seggan.rol.tree.typed.TExpression
import io.github.seggan.rol.tree.typed.TExternDeclaration
import io.github.seggan.rol.tree.typed.TField
import io.github.seggan.rol.tree.typed.TFieldInit
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
import io.github.seggan.rol.tree.typed.TStruct
import io.github.seggan.rol.tree.typed.TStructInit
import io.github.seggan.rol.tree.typed.TVarAssign
import io.github.seggan.rol.tree.typed.TVarDef
import io.github.seggan.rol.tree.typed.TVariableAccess
import io.github.seggan.rol.tree.untyped.UAccess
import io.github.seggan.rol.tree.untyped.UBinaryExpression
import io.github.seggan.rol.tree.untyped.UBinaryOperator
import io.github.seggan.rol.tree.untyped.UBooleanLiteral
import io.github.seggan.rol.tree.untyped.UExpression
import io.github.seggan.rol.tree.untyped.UExternDeclaration
import io.github.seggan.rol.tree.untyped.UFn
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
import io.github.seggan.rol.tree.untyped.UStruct
import io.github.seggan.rol.tree.untyped.UStructInit
import io.github.seggan.rol.tree.untyped.UVarAssign
import io.github.seggan.rol.tree.untyped.UVarDef
import io.github.seggan.rol.tree.untyped.UVariableAccess

class TypeChecker(
    private val dependencyManager: DependencyManager,
    private val imports: Set<String>,
    private val pkg: String
) {

    private val stack = ArrayDeque<StackFrame>()
    private val currentFrame get() = stack.first()
    private val typedFunctions = mutableSetOf<FunctionHeader>()
    private val structs = mutableMapOf<UStruct, TStruct>()

    fun typeAst(ast: UStatements): TStatements {
        val current = ast.children.toMutableList()
        val flat = ast.flatten()
        for (node in flat.filterIsInstance<UStruct>()) {
            structs[node] = typeStruct(node)
        }
        for (node in flat.filterIsInstance<UFn>()) {
            typedFunctions.add(
                FunctionHeader(
                    node.name.name,
                    node.args.map { it.copy(type = locateType(it.type, it.location)) },
                    locateType(node.type, node.location)
                )
            )
        }
        return typeStatements(UStatements(current))
    }

    private fun type(node: UNode): TNode {
        return when (node) {
            is UStatements -> typeStatements(node)
            is UExpression -> typeExpression(node)
            is UVarDef -> typeVariableDeclaration(node).also { currentFrame.vars.add(it) }
            is UVarAssign -> typeVariableAssignment(node)
            is UExternDeclaration -> typeExternDeclaration(node)
            is UFunctionDeclaration -> typeFunctionDeclaration(node)
            is UStruct -> structs[node]!!
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
        val args = node.args.map { it.copy(type = locateType(it.type, it.location)) }
        val type = locateType(node.type, node.location)
        return TFunctionDeclaration(
            node.name.copy(pkg = pkg),
            args,
            type,
            node.modifiers,
            typeStatements(node.body, args.map {
                TVarDef(
                    it.name, it.type, Modifiers(
                        AccessModifier.PRIVATE,
                        const = true
                    ), it.location
                )
            }, type),
            node.location
        )
    }

    private fun typeStruct(node: UStruct): TStruct {
        return TStruct(
            node.name.copy(pkg = pkg),
            node.fields.map {
                if (it.name == "__clazz") Errors.genericError(
                    "Illegal name",
                    "Cannot use __clazz as a field name",
                    it.location
                )
                TField(it.name, it.type, it.modifiers, it.location)
            },
            node.modifiers,
            node.location
        )
    }

    private fun typeExternDeclaration(declaration: UExternDeclaration): TExternDeclaration {
        return TExternDeclaration(
            declaration.name.copy(pkg = pkg),
            declaration.args.map { it.copy(type = locateType(it.type, it.location)) },
            declaration.modifiers,
            declaration.body,
            locateType(declaration.type, declaration.location),
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
            is UStructInit -> typeStructInit(expr)
            is UAccess -> typeAccess(expr)
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

    private fun typeAccess(expr: UAccess): TAccess {
        val target = typeExpression(expr.target)
        if (target.type.isPrimitive) Errors.genericError(
            "Illegal access",
            "Cannot access fields of primitive types",
            expr.location
        )
        val field = getStruct(target.type.name).fields[expr.name] ?: Errors.undefinedReference(expr.name, expr.location)
        return TAccess(target, expr.name, field, expr.location)
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
            return TVarDef(name, locateType(type, decl.location), decl.modifiers, decl.location)
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
        if (name.pkg != null) {
            val returnType = isIn(name.pkg, name.name, args.map(TNode::type))
            if (returnType != null) {
                return TFunctionCall(name.copy(pkg = name.pkg), args, returnType, call.location)
            }
        } else {
            funcLoop@ for (func in typedFunctions.filter { it.name == name.name }) {
                if (func.args.size == args.size) {
                    for (i in args.indices) {
                        if (!func.args[i].type.isAssignableFrom(args[i].type)) {
                            continue@funcLoop
                        }
                    }
                    return TFunctionCall(name.copy(pkg = pkg), args, func.returnType, call.location)
                }
            }
            for (import in imports) {
                val returnType = isIn(import, name.name, args.map(TNode::type))
                if (returnType != null) {
                    return TFunctionCall(name.copy(pkg = import), args, returnType, call.location)
                }
            }
        }
        val errorName = name.toString() + args.joinToString(", ", "(", ")") { it.type.toString() }
        Errors.undefinedReference(errorName, call.location)
    }

    private fun typeStructInit(init: UStructInit): TStructInit {
        val name = locateType(Type(init.name), init.location).name
        val struct = getStruct(name)
        var initCount = 0
        val fields = init.fields.map {
            val expr = checkVoid(typeExpression(it.value))
            val expected = struct.fields[it.name] ?: Errors.undefinedReference(it.name, it.location)
            if (expected.isAssignableFrom(expr.type)) {
                initCount++
                TFieldInit(it.name, expr, it.location)
            } else {
                Errors.typeMismatch(expected, expr.type, expr.location)
            }
        }
        if (initCount != struct.fields.size) {
            Errors.genericError(
                "Struct initialization",
                "Not all fields of struct $name are initialized",
                init.location
            )
        }
        return TStructInit(name, fields, init.location)
    }

    private fun isIn(pack: String, name: String, args: List<Type>): Type? {
        for (dep in dependencyManager.getPackage(pack)) {
            val func = dep.findFunction(name, args)
            if (func != null) {
                dependencyManager.usedDependencies.add(dep)
                return func.returnType
            }
        }
        return null
    }

    private fun typeStatements(
        statements: UStatements,
        extraVars: List<TVarDef> = emptyList(),
        returnType: Type = if (stack.isEmpty()) Type.VOID else currentFrame.returnType
    ): TStatements {
        val vars = if (stack.isEmpty()) mutableListOf() else currentFrame.vars
        vars.addAll(extraVars)
        stack.addFirst(StackFrame(vars, statements, returnType))
        return TStatements(statements.children.map(::type), statements.location).also { stack.removeFirst() }
    }

    private fun locateType(t: Type, location: Location): Type {
        if (t.isPrimitive) return t
        if (t.name.pkg != null) {
            for (dep in dependencyManager.getPackage(t.name.pkg)) {
                val found = dep.findStruct(t.name.name)
                if (found != null) {
                    dependencyManager.usedDependencies.add(dep)
                    return t // the struct is already qualified
                }
            }
        } else {
            for (s in structs.values) {
                if (s.name.name == t.name.name) {
                    return t.copy(name = t.name.copy(pkg = pkg))
                }
            }
            for (import in imports) {
                for (dep in dependencyManager.getPackage(import)) {
                    val found = dep.findStruct(t.name.name)
                    if (found != null) {
                        dependencyManager.usedDependencies.add(dep)
                        return t.copy(name = t.name.copy(pkg = import))
                    }
                }
            }
        }
        Errors.undefinedReference(t.name.toString(), location)
    }

    private fun getStruct(fqname: Identifier): Struct {
        if (fqname.pkg == pkg) {
            return structs.values.first { it.name == fqname }
        } else {
            for (dep in dependencyManager.getPackage(fqname.pkg!!)) {
                val found = dep.findStruct(fqname.name)
                if (found != null) {
                    dependencyManager.usedDependencies.add(dep)
                    return found
                }
            }
            throw AssertionError("Struct $fqname not found") // should never happen
        }
    }
}

private fun checkVoid(expr: TExpression): TExpression {
    if (expr is TFunctionCall && expr.type == Type.VOID) {
        Errors.genericError(
            "Type inference",
            "The non-returning function ${expr.field} cannot be used in an expression",
            expr.location
        )
    }
    return expr
}

private data class StackFrame(val vars: MutableList<TVarDef>, val statements: UStatements, val returnType: Type)

private data class FunctionHeader(val name: String, val args: List<Argument>, val returnType: Type)