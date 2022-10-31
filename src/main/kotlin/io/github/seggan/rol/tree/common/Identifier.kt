package io.github.seggan.rol.tree.common

import io.github.seggan.rol.antlr.RolParser

data class Identifier(val name: String, val pkg: String? = null) {

    companion object {
        fun fromNode(node: RolParser.IdentifierContext): Identifier {
            return Identifier(node.name.text, node.package_()?.text)
        }
    }

    override fun toString(): String {
        return if (pkg == null) {
            name
        } else {
            "$name/$pkg"
        }
    }
}
