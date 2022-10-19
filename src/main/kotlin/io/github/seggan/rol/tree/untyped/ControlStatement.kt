package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.Location

sealed class UControlStatement(children: List<UNode>, location: Location) : UNode(children, location)

class UIfStatement(val cond: UExpression, val ifTrue: UStatements, val ifFalse: UStatements? = null, location: Location) :
    UControlStatement(listOfNotNull(cond, ifTrue, ifFalse), location) {
    override fun toString(): String {
        return if (ifFalse == null) {
            "IfStatement($cond, $ifTrue)"
        } else {
            "IfStatement($cond, $ifTrue, $ifFalse)"
        }
    }
}

class UMatchStatement(val cond: UExpression, val cases: List<UMatchCase>, location: Location) :
    UControlStatement(listOf(cond) + cases, location) {
    override fun toString(): String {
        return "MatchStatement($cond, $cases)"
    }
}

class UMatchCase(val cond: UExpression, val body: UNode, location: Location) : UNode(listOf(cond, body), location) {
    override fun toString(): String {
        return "MatchCase($cond, $body)"
    }
}

class UWhileStatement(val cond: UExpression, val body: UStatements, location: Location) :
    UControlStatement(listOf(cond, body), location) {
    override fun toString(): String {
        return "WhileStatement($cond, $body)"
    }
}

// TODO: ForStatement

class UForEachStatement(val varName: String, val iterable: UExpression, val body: UStatements, location: Location) :
    UControlStatement(listOf(iterable, body), location) {
    override fun toString(): String {
        return "ForEachStatement($varName, $iterable, $body)"
    }
}