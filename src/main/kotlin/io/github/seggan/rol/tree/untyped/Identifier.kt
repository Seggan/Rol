package io.github.seggan.rol.tree.untyped

sealed class Identifier(val name: String, children: List<Node>) : Expression(children)

class VariableAccess(name: String) : Identifier(name, emptyList()) {
    override fun toString(): String {
        return "VariableAccess($name)"
    }
}

class FunctionCall(name: String, val args: List<Expression>) : Identifier(name, args) {
    override fun toString(): String {
        return "FunctionCall($name, ${args.joinToString(", ")})"
    }
}

class Access(val obj: Expression, name: String) : Identifier(name, listOf(obj)) {
    override fun toString(): String {
        return "Access($obj, $name)"
    }
}