package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Location

sealed class UNode(val children: List<UNode>, val location: Location) {
    override fun toString(): String {
        return "${this::class.simpleName}(${children.joinToString(", ")})"
    }

    fun flatten(): List<UNode> {
        return listOf(this) + children.flatMap { it.flatten() }
    }

    open fun shallowFlatten(): List<UNode> {
        return listOf(this) + children.flatMap { it.shallowFlatten() }
    }

    inline fun <reified T> childrenOfType(): List<T> {
        return children.filterIsInstance<T>()
    }
}

class UStatements(children: List<UNode>) : UNode(children, children.first().location) {
    constructor(vararg children: UNode) : this(children.toList())
}