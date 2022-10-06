package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.Location

sealed class Identifier(val name: String, children: List<UNode>, location: Location) : UExpression(children, location)

class VariableAccess(name: String, location: Location) : Identifier(name, emptyList(), location) {
    override fun toString(): String {
        return "VariableAccess($name)"
    }
}

class FunctionCall(name: String, val args: List<UExpression>, location: Location) : Identifier(name, args, location) {
    override fun toString(): String {
        return "FunctionCall($name, ${args.joinToString(", ")})"
    }
}

class Access(val obj: UExpression, name: String, location: Location) : Identifier(name, listOf(obj), location) {
    override fun toString(): String {
        return "Access($obj, $name)"
    }
}