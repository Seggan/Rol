package io.github.seggan.rol.tree.common

interface Reference {
    val name: String
}

interface Struct {
    val name: Identifier
    val fields: Map<String, Type>
    val const: Boolean
}