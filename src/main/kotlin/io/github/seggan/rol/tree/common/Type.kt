package io.github.seggan.rol.tree.common

import io.github.seggan.rol.antlr.RolParser

private val PRIMITIVE_TYPES = setOf(
    "Num",
    "String",
    "Boolean",
    "dyn",
    "<nothing>"
)

data class Type(val name: Identifier, val nullable: Boolean = false) {

    constructor(name: String, nullable: Boolean = false) : this(Identifier(name), nullable)

    val isPrimitive = name.name in PRIMITIVE_TYPES

    companion object {
        val NUMBER = Type("Num")
        val STRING = Type("String")
        val BOOLEAN = Type("Boolean")

        // The three special types
        val DYNAMIC = Type("dyn")
        val ANY = Type("dyn", true)

        // Special type for the absence of type
        val VOID = Type("<nothing>")

        fun parse(str: String): Type {
            return Type(Identifier.parseString(str.removeSuffix("?")), str.endsWith("?"))
        }

        fun parse(node: RolParser.TypeContext): Type {
            if (node.DYN() != null) {
                return if (node.QUESTION() == null) ANY else DYNAMIC
            }
            return Type(Identifier.fromNode(node.identifier()), node.QUESTION() != null)
        }

        fun struct(name: Identifier): Type {
            return Type(name)
        }
    }

    override fun toString(): String {
        return if (nullable) "$name?" else name.toString()
    }

    fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other.nullable -> nullable && copy(nullable = false).isAssignableFrom(other.copy(nullable = false))
            nullable -> copy(nullable = false).isAssignableFrom(other.copy(nullable = false))
            this == DYNAMIC || other == DYNAMIC -> true
            this == VOID || other == VOID -> false
            else -> this == other
        }
    }
}

fun RolParser.TypeContext.toType(): Type {
    return Type.parse(this)
}