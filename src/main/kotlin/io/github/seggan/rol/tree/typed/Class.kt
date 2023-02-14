package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.ConcreteType
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type

class TClass(
    val cType: ConcreteType,
    val fields: List<TField>,
    location: Location
) : TNode(cType, listOf(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        TODO("Not yet implemented")
    }
}

class TField(
    val name: String,
    type: Type,
    val value: TExpression? = null,
    location: Location
) : TNode(type, listOfNotNull(value), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        TODO("Not yet implemented")
    }
}