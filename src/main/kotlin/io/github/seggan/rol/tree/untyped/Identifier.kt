package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location

sealed class UIdentifier(val name: Identifier, children: List<UNode>, location: Location) :
    UExpression(children, location)

class UVariableAccess(name: Identifier, location: Location) : UIdentifier(name, emptyList(), location) {
    override fun toString(): String {
        return "VariableAccess($name)"
    }
}

class UCall(val expr: UExpression, val args: List<UExpression>, location: Location) :
    UExpression(args + expr, location) {
    override fun toString(): String {
        return "FunctionCall($expr, ${args.joinToString(", ")})"
    }
}

class UAccess(val target: UExpression, name: String, location: Location) :
    UIdentifier(Identifier(name), listOf(target), location) {
    override fun toString(): String {
        return "Access($target, $name)"
    }
}

class UFieldSet(val target: List<String>, val value: UExpression, location: Location) :
    UNode(listOf(value), location) {
    override fun toString(): String {
        return "FieldSet($target, $value)"
    }
}