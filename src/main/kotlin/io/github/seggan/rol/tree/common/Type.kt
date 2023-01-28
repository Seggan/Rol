package io.github.seggan.rol.tree.common

private val PRIMITIVE_TYPES = setOf(
    "Num",
    "String",
    "Boolean",
    "dyn",
    "<nothing>"
)

sealed class Type(val name: Identifier, val nullable: Boolean = false) {

    abstract fun nonNullable(): Type
    abstract fun nullable(): Type

    /**
     * Can [other] be assigned to this type?
     */
    open fun isAssignableFrom(other: Type): Boolean {
        return when {
            this === other -> true
            other.nullable -> nullable && nonNullable().isAssignableFrom(other.nonNullable())
            nullable -> nonNullable().isAssignableFrom(other.nonNullable())
            this == DynType || other == DynType -> true
            this == VoidType || other == VoidType -> false
            else -> this == other
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Type) return false
        return name == other.name && nullable == other.nullable
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + nullable.hashCode()
        return result
    }
}

class ConcreteType(
    name: Identifier,
    nullable: Boolean = false,
    superclass: ConcreteType? = null,
    val interfaces: List<InterfaceType> = listOf()
) : Type(name, nullable) {

    val superclass: ConcreteType? = if (this == OBJECT) null else (superclass ?: OBJECT)

    companion object {
        val OBJECT = ConcreteType(Identifier("Object"))
        val NUM = ConcreteType(Identifier("Num"))
        val STRING = ConcreteType(Identifier("String"))
        val BOOLEAN = ConcreteType(Identifier("Boolean"))
    }

    override fun nonNullable(): Type = if (nullable) ConcreteType(name) else this
    override fun nullable(): Type = if (nullable) this else ConcreteType(name, true)

    fun isSubclassOf(other: ConcreteType): Boolean {
        return when {
            this == other -> true
            superclass != null -> superclass.isSubclassOf(other)
            else -> false
        }
    }

    fun isSubclassOf(other: InterfaceType): Boolean {
        return when {
            interfaces.flatMapTo(mutableSetOf()) { setOf(it) + it.superInterfaces }.contains(other) -> true
            superclass != null -> superclass.isSubclassOf(other)
            else -> false
        }
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            super.isAssignableFrom(other) -> true
            other is ConcreteType -> isSubclassOf(other)
            other is InterfaceType -> isSubclassOf(other)
            else -> false
        }
    }
}

class InterfaceType(name: Identifier, nullable: Boolean = false, val extends: List<InterfaceType> = listOf()) :
    Type(name, nullable) {

    val superInterfaces: Set<InterfaceType> = extends.flatMapTo(mutableSetOf()) { setOf(it) + it.superInterfaces }

    override fun nonNullable(): Type = if (nullable) InterfaceType(name) else this
    override fun nullable(): Type = if (nullable) this else InterfaceType(name, true)

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            super.isAssignableFrom(other) -> true
            other is InterfaceType -> superInterfaces.contains(other)
            else -> false
        }
    }
}

object DynType : Type(Identifier("dyn")) {
    override fun nonNullable(): Type = this
    override fun nullable(): Type = AnyType
}

object AnyType : Type(Identifier("dyn"), true) {
    override fun nonNullable(): Type = DynType
    override fun nullable(): Type = this
}

object VoidType : Type(Identifier("<nothing>")) {
    override fun nonNullable(): Type = this
    override fun nullable(): Type = this
}