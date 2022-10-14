package io.github.seggan.rol.tree.lua

sealed class LNode {

    abstract fun transpile(): String
}

class LStatements(private val statements: List<LNode>, private val indent: Int = 0) : LNode() {
    override fun transpile(): String {
        return statements.filter { it !is LNop }.joinToString("\n") { "\t".repeat(indent) + it.transpile() }
    }

    fun withIndent(indent: Int) = LStatements(statements, indent)
}

class LString(private val string: String) : LNode() {
    override fun transpile(): String {
        return "\"$string\""
    }
}

class LLiteral(val value: String) : LNode() {
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

class LFunctionCall(val name: String, private val args: List<LNode>) : LNode() {

    constructor(name: String, vararg args: LNode) : this(name, args.toList())

    override fun transpile(): String {
        return "$name(${args.joinToString(", ") { it.transpile() }})"
    }
}

class LBinaryExpression(private val left: LNode, private val operator: String, val right: LNode) : LNode() {
    override fun transpile(): String {
        return "(${left.transpile()} $operator ${right.transpile()})"
    }
}

class LUnaryExpression(private val operator: String, private val value: LNode) : LNode() {
    override fun transpile(): String {
        return "($operator${value.transpile()})"
    }
}

class LFunctionDefinition(val name: String, val args: List<String>, val body: LStatements) : LNode() {

    override fun transpile(): String {
        return "function $name(${args.joinToString(", ")})\n${body.transpile()}\nend"
    }
}

object LNop : LNode() {
    override fun transpile(): String {
        return ""
    }
}