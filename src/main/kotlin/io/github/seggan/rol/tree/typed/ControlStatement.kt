package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type

sealed class ControlStatement(children: List<TNode>, location: Location) : TNode(Type.VOID, children, location)

class TIfStatement(
    val condition: TExpression,
    val ifBody: TStatements,
    val elseBody: TStatements?,
    location: Location
) : ControlStatement(listOfNotNull(condition, ifBody, elseBody), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitIfStatement(this)
    }
}
