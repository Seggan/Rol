package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.AccessModifier
import io.github.seggan.rol.tree.Location

sealed class UFn(val name: String, val args: List<UArgument>, children: List<UNode> = emptyList(), location: Location) :
    UNode(children, location)

class UFunctionDeclaration(name: String, args: List<UArgument>, val access: AccessModifier, val body: UStatements, location: Location) :
    UFn(name, args, listOf(body), location) {
    override fun toString(): String {
        return "FunctionDeclaration($name, $args, $body)"
    }
}

class UExternDeclaration(name: String, val mappedName: String, args: List<UArgument>, location: Location) :
    UFn(name, args, listOf(), location) {
    override fun toString(): String {
        return "ExternDeclaration($name, $mappedName, $args)"
    }
}

class UArgument(name: String, val type: UTypename?, location: Location) : UVar(name, listOf(), location)