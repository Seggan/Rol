package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.tree.Location
import io.github.seggan.rol.tree.location
import io.github.seggan.rol.tree.typed.Type
import org.jetbrains.annotations.Contract

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

class UTypename(val name: String, val isNullable: Boolean = false, location: Location) : UNode(emptyList(), location) {
    companion object {
        @Contract("null -> null")
        fun parse(type: RolParser.TypeContext?): UTypename? {
            if (type == null || type.text.isBlank()) {
                return null
            }
            val text = type.text
            return UTypename(text.removeSuffix("?"), text.endsWith("?"), type.location)
        }
    }

    fun toType(): Type {
        return Type(name, isNullable)
    }

    override fun toString(): String {
        return name + if (isNullable) "?" else ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UTypename) return false
        return name == other.name && isNullable == other.isNullable
    }

    override fun hashCode(): Int {
        return name.hashCode() * 31 + isNullable.hashCode()
    }
}