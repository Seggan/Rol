package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Type

class UStructDef(
    val name: Identifier,
    val modifiers: Modifiers,
    val fields: List<UFieldDef>,
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