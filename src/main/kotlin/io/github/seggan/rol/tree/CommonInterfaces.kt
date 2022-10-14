package io.github.seggan.rol.tree

import io.github.seggan.rol.tree.typed.Type

interface Reference {
    val name: String
}

interface Argument {
    val name: String
    val type: Type
}