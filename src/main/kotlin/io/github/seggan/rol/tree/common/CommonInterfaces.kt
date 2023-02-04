package io.github.seggan.rol.tree.common

interface Reference {
    val name: String
}

interface AClass {
    val name: Identifier
    val fields: Map<String, Type>
}
