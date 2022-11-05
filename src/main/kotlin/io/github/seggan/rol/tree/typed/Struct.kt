package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Struct
import io.github.seggan.rol.tree.common.Type

class TStruct(override val name: Identifier, val fieldNodes: List<TField>, val modifiers: Modifiers, location: Location) :
    TNode(Type.VOID, fieldNodes, location), Struct {

    override val fields = fieldNodes.associate { it.name to it.type }
    override val const = modifiers.const

    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitStructDeclaration(this)
    }
}

class TField(val name: String, type: Type, val modifiers: Modifiers, location: Location) :
    TNode(type, listOf(), location) {

    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitField(this)
    }
}

class TStructInit(val name: Identifier, val fields: List<TFieldInit>, location: Location) :
    TExpression(Type.struct(name), fields, location) {

    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitStructInit(this)
    }
}

class TFieldInit(val name: String, val value: TExpression, location: Location) :
    TExpression(value.type, listOf(value), location) {

    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitFieldInit(this)
    }
}