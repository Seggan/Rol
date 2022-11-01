package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Argument
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Type

sealed class UFn(
    val name: Identifier,
    val args: List<Argument>,
    val modifiers: Modifiers,
    val type: Type,
    children: List<UNode> = emptyList(),
    location: Location
) :
    UNode(children, location)

class UFunctionDeclaration(
    name: Identifier,
    args: List<Argument>,
    modifiers: Modifiers,
    val body: UStatements,
    type: Type,
    location: Location
) :
    UFn(name, args, modifiers, type, listOf(body), location) {
    override fun toString(): String {
        return "FunctionDeclaration($name, $args, $body)"
    }

    override fun shallowFlatten(): List<UNode> {
        return listOf(this)
    }
}

class UExternDeclaration(
    name: Identifier,
    args: List<Argument>,
    modifiers: Modifiers,
    val body: String,
    type: Type,
    location: Location
) :
    UFn(name, args, modifiers, type, listOf(), location) {
    override fun toString(): String {
        return "ExternDeclaration($name, $args, $body)"
    }
}

class UReturn(val value: UExpression?, location: Location) : UNode(listOfNotNull(value), location) {

    override fun toString() = "return $value"

}