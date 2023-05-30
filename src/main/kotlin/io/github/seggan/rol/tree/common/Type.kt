package io.github.seggan.rol.tree.common

import io.github.seggan.rol.antlr.RolLexer
import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.antlr.RolParserBaseVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

sealed class Type(val name: Identifier, val nullable: Boolean = false) {

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
    val fields: Map<String, Type>,
    val traits: List<TraitType> = listOf(),
    nullable: Boolean = false,
) : ResolvedType(name, nullable) {

    companion object {
        val NUMBER = ConcreteType(Identifier("Number"), mapOf())
        val STRING = ConcreteType(Identifier("String"), mapOf())
        val BOOLEAN = ConcreteType(Identifier("Boolean"), mapOf())
    }

    override fun nonNullable(): ConcreteType = if (nullable) copy(nullable = false) else this
    override fun nullable(): ConcreteType = if (nullable) this else copy(nullable = true)

    override fun withNullability(nullable: Boolean): ConcreteType {
        return if (nullable) nullable() else nonNullable()
    }

    fun copy(
        name: Identifier = this.name,
        fields: Map<String, Type> = this.fields,
        traits: List<TraitType> = this.traits,
        nullable: Boolean = this.nullable
    ): ConcreteType {
        return ConcreteType(name, fields, traits, nullable)
    }
}

class TraitType(name: Identifier, nullable: Boolean = false, val extends: List<TraitType> = listOf()) :
    ResolvedType(name, nullable) {

    val superTraits: Set<TraitType> = extends.flatMapTo(mutableSetOf()) { setOf(it) + it.superTraits }

    override fun nonNullable(): TraitType = if (nullable) TraitType(name) else this
    override fun nullable(): TraitType = if (nullable) this else TraitType(name, true)

    override fun withNullability(nullable: Boolean): TraitType {
        return if (nullable) nullable() else nonNullable()
    }

    override fun isAssignableFrom(other: Type): Boolean {
        return when {
            super.isAssignableFrom(other) -> true
            other is TraitType -> other.superTraits.contains(this)
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

class FunctionType(
    val args: List<Type>,
    val returnType: Type,
    val receiverType: Type? = null,
    nullable: Boolean = false
) :
    Type(Identifier(toString()), nullable) {

    // For some reason Kotlin complains about the above toString call if there is no companion object
    companion object;

    override fun nonNullable(): FunctionType = if (nullable) FunctionType(args, returnType, receiverType) else this
    override fun nullable(): FunctionType = if (nullable) this else FunctionType(args, returnType, receiverType, true)

    override fun isAssignableFrom(other: Type): Boolean {
        if (super.isAssignableFrom(other)) return true
        if (other !is FunctionType) return false
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
    return TypeWalker.visit(this)
}


fun Identifier.toType(): Type {
    return this.toString().toType()
}

fun String.toType(): Type {
    val lexer = RolLexer(CharStreams.fromString(this))
    val parser = RolParser(CommonTokenStream(lexer))
    return parser.fullType().type().toType()
}

private object TypeWalker : RolParserBaseVisitor<Type>() {

    override fun visitFunctionType(ctx: RolParser.FunctionTypeContext): Type {
        val args = ctx.args.map(::visit)
        val returnType = visit(ctx.returnType)
        val receiverType = ctx.recvType()?.let(::visit)
        return FunctionType(args, returnType, receiverType)
    }

    override fun visitNullableType(ctx: RolParser.NullableTypeContext): Type {
        return visitChildren(ctx).nullable()
    }

    override fun visitTypeName(ctx: RolParser.TypeNameContext): Type {
        if (ctx.DYN() != null) return DynType
        val id = ctx.identifier().toIdentifier()
        val name = id.name
        if (id.pkg == null) {
            when (name) {
                ConcreteType.NUMBER.name.name -> return ConcreteType.NUMBER
                ConcreteType.STRING.name.name -> return ConcreteType.STRING
                ConcreteType.BOOLEAN.name.name -> return ConcreteType.BOOLEAN
                VoidType.name.name -> return VoidType
            }
        }
        return UnresolvedType(id)
    }

    override fun aggregateResult(aggregate: Type?, nextResult: Type?): Type {
        return nextResult ?: aggregate ?: throw IllegalStateException("No type found")
    }
}

open class A(s: String)

class B : A(this.toString()) {
    companion object
}