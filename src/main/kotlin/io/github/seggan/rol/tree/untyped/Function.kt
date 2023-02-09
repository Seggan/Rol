package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Argument
import io.github.seggan.rol.tree.common.FunctionType
import io.github.seggan.rol.tree.common.Location

class ULambda(
    val args: List<Argument>,
    val body: UStatements,
    val type: FunctionType?,
    location: Location
) : UExpression(listOf(body), location) {
    override fun toString(): String {
        return "Lambda($args, $body)"
    }
}

class UReturn(val value: UExpression?, location: Location) : UNode(listOfNotNull(value), location) {

    override fun toString() = "return $value"

}