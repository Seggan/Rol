package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type

class UClassDef(
    val name: Identifier,
    val fields: List<UFieldDef>,
    val superstuff: List<Identifier>,
    location: Location
) : UNode(fields, location) {
    override fun toString(): String {
        return "Class($name, $fields)"
    }
}

class UFieldDef(
    val name: Identifier,
    val type: Type?,
    location: Location
) : UNode(listOf(), location) {
    override fun toString(): String {
        return "FieldDef($name, $type)"
    }
}