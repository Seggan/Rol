package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Argument
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Type

sealed class UFn(val name: String, val args: List<Argument>, children: List<UNode> = emptyList(), location: Location) :
    UNode(children, location)

class UFunctionDeclaration(
    name: String,
    args: List<Argument>,
    val modifiers: Modifiers,
    val body: UStatements,
    val type: Type,
    location: Location
) :
    UFn(name, args, listOf(body), location) {
    override fun toString(): String {
        return "FunctionDeclaration($name, $args, $body)"
    }

    override fun shallowFlatten(): List<UNode> {
        return listOf(this)
    }
}

class UExternDeclaration(name: String, val nativeName: String, args: List<Argument>, location: Location) :
    UFn(name, args, listOf(), location) {
    override fun toString(): String {
        return "ExternDeclaration($name, $nativeName, $args)"
    }
}

class UReturn(val value: UExpression?, location: Location) : UNode(listOfNotNull(value), location) {

    override fun toString() = "return $value"

}