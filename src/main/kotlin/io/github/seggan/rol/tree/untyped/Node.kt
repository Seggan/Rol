package io.github.seggan.rol.tree.untyped

sealed class Node(val children: List<Node>) {
    override fun toString(): String {
        return "${this::class.simpleName}(${children.joinToString(", ")})"
    }

    fun flatten(): List<Node> {
        return listOf(this) + children.flatMap { it.flatten() }
    }

    inline fun <reified T> childrenOfType(): List<T> {
        return children.filterIsInstance<T>()
    }
}

class Statements(children: List<Node>) : Node(children) {
    constructor(vararg children: Node) : this(children.toList())
}

class Typename(val name: String, val isNullable: Boolean = false) : Node(emptyList()) {
    companion object {
        fun parse(str: String): Typename {
            return Typename(str.removeSuffix("?"), str.endsWith("?"))
        }
    }

    override fun toString(): String {
        return name + if (isNullable) "?" else ""
    }
}