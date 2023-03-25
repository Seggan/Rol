

package io.github.seggan.rol.tree.common

import io.github.seggan.rol.antlr.RolParser

data class Identifier(val name: String, val pkg: String? = null) {

    companion object {

        private const val SEPARATOR = ':'

        fun parseString(string: String): Identifier {
            val split = string.split(SEPARATOR)
            return Identifier(split.last(), if (split.size == 2) split.first() else null)
        }
    }

    override fun toString(): String {
        return if (pkg == null) name else "$pkg$SEPARATOR$name"
    }
}

fun RolParser.IdentifierContext.toIdentifier(): Identifier {
    return Identifier(this.id().text, this.package_()?.text)
}

fun RolParser.IdContext.toIdentifier(): Identifier {
    return Identifier(this.text)
}
