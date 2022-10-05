package io.github.seggan.rol.tree.untyped

sealed class ControlStatement(children: List<Node>) : Node(children)

class IfStatement(val cond: Expression, val ifTrue: Statements, val ifFalse: Statements? = null) :
    ControlStatement(listOfNotNull(cond, ifTrue, ifFalse)) {
    override fun toString(): String {
        return if (ifFalse == null) {
            "IfStatement($cond, $ifTrue)"
        } else {
            "IfStatement($cond, $ifTrue, $ifFalse)"
        }
    }
}

class MatchStatement(val cond: Expression, val cases: List<MatchCase>) :
    ControlStatement(listOf(cond) + cases) {
    override fun toString(): String {
        return "MatchStatement($cond, $cases)"
    }
}

data class MatchCase(val cond: Expression, val body: Node) : Node(listOf(cond, body)) {
    override fun toString(): String {
        return "MatchCase($cond, $body)"
    }
}

class WhileStatement(val cond: Expression, val body: Statements) : ControlStatement(listOf(cond, body)) {
    override fun toString(): String {
        return "WhileStatement($cond, $body)"
    }
}

// TODO: ForStatement

class ForEachStatement(val varName: String, val iterable: Expression, val body: Statements) :
    ControlStatement(listOf(iterable, body)) {
    override fun toString(): String {
        return "ForEachStatement($varName, $iterable, $body)"
    }
}

class ReturnStatement(val value: Expression? = null) : ControlStatement(listOfNotNull(value)) {
    override fun toString(): String {
        return "ReturnStatement($value)"
    }
}