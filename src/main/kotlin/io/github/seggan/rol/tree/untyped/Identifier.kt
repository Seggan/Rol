package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Reference

sealed class UIdentifier(val name: String, children: List<UNode>, location: Location) : UExpression(children, location)

class UVariableAccess(name: String, location: Location) : UIdentifier(name, emptyList(), location), Reference {
    override fun toString(): String {
        return "VariableAccess($name)"
    }
}

class UFunctionCall(val fname: Identifier, val args: List<UExpression>, location: Location) :
    UIdentifier(fname.name, args, location), Reference {
    override fun toString(): String {
        return "FunctionCall($name, ${args.joinToString(", ")})"
    }
}

class UAccess(val target: UExpression, name: String, location: Location) : UIdentifier(name, listOf(target), location) {
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