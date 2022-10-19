package io.github.seggan.rol.tree.common

data class Argument(val name: String, val type: Type, val location: Location) {
    override fun toString() = "$name: $type"
}
