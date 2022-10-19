package io.github.seggan.rol.tree.common

import io.github.seggan.rol.antlr.RolParser

enum class AccessModifier {
    PUBLIC,
    PACKAGE,
    PRIVATE;

    companion object {
        fun parse(mod: RolParser.AccessModifierContext?): AccessModifier {
            if (mod == null) {
                return PUBLIC
            }
            return valueOf(mod.text.uppercase())
        }
    }
}