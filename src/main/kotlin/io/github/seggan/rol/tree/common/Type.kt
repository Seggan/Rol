package io.github.seggan.rol.tree.common

data class Type(val name: String, val nullable: Boolean = false) {

    companion object {
        val NUMBER = Type("Num")
        val STRING = Type("String")
        val BOOLEAN = Type("Boolean")

        // The three special types
        val DYNAMIC = Type("dyn")
        val ANY = Type("dyn", true)
        val VOID = Type("")

        fun parse(str: String): Type {
            return Type(str.removeSuffix("?"), str.endsWith("?"))
        }
    }

    override fun toString(): String {
        return if (nullable) "$name?" else name
    }

    fun isAssignableFrom(other: Type): Boolean {
        return when {
            other.nullable -> nullable && copy(nullable = false).isAssignableFrom(other.copy(nullable = false))
            nullable -> copy(nullable = false).isAssignableFrom(other.copy(nullable = false))
            this == DYNAMIC || other == DYNAMIC -> true
            this == VOID || other == VOID -> false
            else -> this == other
        }
    }
}