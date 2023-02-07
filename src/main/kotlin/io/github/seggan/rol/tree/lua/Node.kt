package io.github.seggan.rol.tree.lua

sealed class LNode {

    abstract fun transpile(): String
}

class LStatements(private val statements: List<LNode>) : LNode() {
    companion object {
        private val NEWLINE = "\n".toRegex()
    }

    override fun transpile(): String {
        return statements.filter { it !is LNop }.flatMap { NEWLINE.split(it.transpile()) }.filterNot(String::isBlank)
            .joinToString("\n")
    }
}

class LString(private val string: String) : LExpression() {
    override fun transpile(): String {
        return "\"$string\""
    }
}

class LLiteral(val value: String) : LExpression() {
    override fun transpile() = value
}

class LVariableDeclaration(val name: String) : LNode() {
    override fun transpile(): String {
        return "local $name"
    }
}

class LAssignment(val name: String, private val value: LNode) : LNode() {
    override fun transpile(): String {
        return "$name = ${value.transpile()}"
    }
}

sealed class LExpression : LNode()

class LFunctionCall(val expr: LExpression, private val args: List<LExpression>) : LExpression() {

    constructor(expr: LExpression, vararg args: LExpression) : this(expr, args.toList())

    override fun transpile(): String {
        return "${expr.transpile()}(${args.joinToString(", ") { it.transpile() }})"
    }
}

class LBinaryExpression(private val left: LNode, private val operator: String, val right: LNode) : LExpression() {
    override fun transpile(): String {
        return "(${left.transpile()}$operator${right.transpile()})"
    }
}

class LUnaryExpression(private val operator: String, private val value: LNode) : LExpression() {
    override fun transpile(): String {
        return "($operator${value.transpile()})"
    }
}

class LFunction(val args: List<String>, val body: LStatements) : LExpression() {
    override fun transpile(): String {
        return "function (${args.joinToString(", ")})\n${body.transpile()}\nend"
    }
}

class LReturn(val expression: LNode?) : LNode() {
    override fun transpile(): String {
        return "return ${expression?.transpile() ?: ""}"
    }
}

class LClosureCall(val code: LNode) : LNode() {
    override fun transpile(): String {
        return "(function () ${code.transpile()} end)()"
    }
}

class LStructInit(val name: String, val args: Map<String, LNode>) : LExpression() {
    override fun transpile(): String {
        return "{ __clazz = '$name', ${args.map { "${it.key} = ${it.value.transpile()}" }.joinToString(", ")} }"
    }
}

object LNop : LNode() {
    override fun transpile(): String {
        return ""
    }
}