package io.github.seggan.rol.tree.untyped

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Type

class UStruct(val name: Identifier, val fields: List<UField>, val modifiers: Modifiers, location: Location) :
    UNode(listOf(), location)

class UField(name: String, val type: Type, val modifiers: Modifiers, location: Location) :
    UVar(name, listOf(), location)

class UStructInit(val name: Identifier, val fields: List<UFieldInit>, location: Location) :
    UExpression(fields, location)

class UFieldInit(val name: String, val value: UExpression, location: Location) :
    UExpression(listOf(), location)