package io.github.seggan.rol.tree.common

import io.github.seggan.rol.antlr.RolParser

sealed class Type(val name: Identifier, val nullable: Boolean = false) {

    companion object {

        fun parse(type: String): Type = FunctionType.parse(type) ?: Identifier.parseString(type).toType()
    }

    abstract fun nonNullable(): Type
    abstract fun nullable(): Type

    open fun withNullability(nullable: Boolean): Type {
        return if (nullable) nullable() else nonNullable()
    }

    /**
     * Can [other] be assigned to this type?
     */
    open fun isAssignableFrom(other: Type): Boolean {
        return when {
            this == other -> true
            other.nullable -> nullable && nonNullable().isAssignableFrom(other.nonNullable())
            nullable -> nonNullable().isAssignableFrom(other.nonNullable())
            this == DynType || other == DynType -> true
            this == VoidType || other == VoidType -> false
            else -> false
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

    override fun toString(): String {
        return name.toString() + if (nullable) "?" else ""
    }
}

sealed class ResolvedType(name: Identifier, nullable: Boolean = false) : Type(name, nullable)

class ConcreteType(
    name: Identifier,
    nullable: Boolean = false,
    superclass: ConcreteType? = null,
    val interfaces: List<InterfaceType> = listOf()
) : ResolvedType(name, nullable) {

    val superclass: ConcreteType? = if (this == OBJECT) null else (superclass ?: OBJECT)

    companion object {
        val OBJECT = ConcreteType(Identifier("Object"))
        val NUMBER = ConcreteType(Identifier("Number"))
        val STRING = ConcreteType(Identifier("String"))
        val BOOLEAN = ConcreteType(Identifier("Boolean"))
    }

    override fun nonNullable(): ConcreteType = if (nullable) ConcreteType(name) else this
    override fun nullable(): ConcreteType = if (nullable) this else ConcreteType(name, true)

    override fun withNullability(nullable: Boolean): ConcreteType {
        return if (nullable) nullable() else nonNullable()
    }

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
            other is ConcreteType -> other.isSubclassOf(this)
            else -> false
        }
    }
}

class InterfaceType(name: Identifier, nullable: Boolean = false, val extends: List<InterfaceType> = listOf()) :
    ResolvedType(name, nullable) {

    val superInterfaces: Set<InterfaceType> = extends.flatMapTo(mutableSetOf()) { setOf(it) + it.superInterfaces }

    override fun nonNullable(): InterfaceType = if (nullable) InterfaceType(name) else this
    override fun nullable(): InterfaceType = if (nullable) this else InterfaceType(name, true)

    override fun withNullability(nullable: Boolean): InterfaceType {
        return if (nullable) nullable() else nonNullable()
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            super.isAssignableFrom(other) -> true
            other is InterfaceType -> other.superInterfaces.contains(this)
            else -> false
        }
    }
}

class UnresolvedType(name: Identifier, nullable: Boolean = false) : Type(name, nullable) {

    override fun nonNullable(): UnresolvedType = if (nullable) UnresolvedType(name) else this
    override fun nullable(): UnresolvedType = if (nullable) this else UnresolvedType(name, true)

    override fun toString(): String {
        return "UNRESOLVED/" + super.toString()
    }
}

class FunctionType(val args: List<Type>, val returnType: Type) :
    Type(Identifier(toString())) {

    companion object {
        private val parseRegex = """\((.*)\)\s*->\s*(.*)\??""".toRegex()
        private val commaRegex = """\s*,\s*""".toRegex()

        fun parse(s: String): FunctionType? {
            val type = s.trim()
            val match = parseRegex.matchEntire(type) ?: return null
            val argStr = match.groupValues[1]
            val args = if (argStr.isEmpty()) listOf() else argStr.split(commaRegex).map { Type.parse(it) }
            val returnType = Type.parse(match.groupValues[2])
            return FunctionType(args, returnType)
        }
    }

    override fun nonNullable(): FunctionType = this
    override fun nullable(): FunctionType = this

    override fun isAssignableFrom(other: Type): Boolean {
        if (other !is FunctionType) return false
        if (super.isAssignableFrom(other)) return true
        if (args.size != other.args.size) return false
        return args.zip(other.args)
            .all { (a, b) -> a.isAssignableFrom(b) } && returnType.isAssignableFrom(other.returnType)
    }

    override fun toString(): String {
        return "(${args.joinToString(", ")}) -> $returnType"
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

object VoidType : Type(Identifier("Void")) {
    override fun nonNullable(): Type = this
    override fun nullable(): Type = this
}

fun RolParser.TypeContext.toType(): Type {
    if (DYN() != null) {
        return if (QUESTION() != null) AnyType else DynType
    } else if (functionType() != null) {
        val node = functionType()
        val args = node.args.map { it.toType() }
        val returnType = node.returnType.toType()
        return FunctionType(args, returnType)
    }
    return Type.parse(text)
}


fun Identifier.toType(): Type {
    val nullable = name.endsWith("?")
    val name = name.removeSuffix("?")
    if (pkg == null) {
        return when (name) {
            ConcreteType.NUMBER.name.name -> ConcreteType.NUMBER.withNullability(nullable)
            ConcreteType.STRING.name.name -> ConcreteType.STRING.withNullability(nullable)
            ConcreteType.BOOLEAN.name.name -> ConcreteType.BOOLEAN.withNullability(nullable)
            ConcreteType.OBJECT.name.name -> ConcreteType.OBJECT.withNullability(nullable)
            DynType.name.name -> DynType.withNullability(nullable)
            VoidType.name.name -> VoidType
            else -> UnresolvedType(Identifier(name), nullable)
        }
    }
    return UnresolvedType(Identifier(name, pkg), nullable)
}