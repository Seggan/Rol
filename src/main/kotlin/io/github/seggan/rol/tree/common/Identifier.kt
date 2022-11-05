package io.github.seggan.rol.tree.common

import io.github.seggan.rol.antlr.RolParser

data class Identifier(val name: String, val pkg: String? = null) {

    companion object {
        fun fromNode(node: RolParser.IdentifierContext): Identifier {
            return Identifier(node.name.text, node.package_()?.text)
        }

        fun parseString(string: String): Identifier {
            val split = string.split('/')
            return Identifier(split.last(), split.dropLast(1).joinToString("."))
        }
    }

    override fun toString(): String {
        return if (pkg == null) name else "$pkg/$name"
    }
}
