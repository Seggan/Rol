package io.github.seggan.rol

import io.github.seggan.rol.tree.Location
import io.github.seggan.rol.tree.typed.Type
import kotlin.system.exitProcess

object Errors {

    fun undefinedReference(name: String, location: Location): Nothing {
        genericError("Unknown reference", "$name is not defined", location)
    }

    fun duplicateDefinition(name: String, location: Location): Nothing {
        genericError("Duplicate definition", "$name is already defined", location)
    }

    fun typeMismatch(expected: Type, actual: Type, location: Location): Nothing {
        genericError("Type mismatch", "Expected $expected, got $actual", location)
    }

    fun genericError(type: String, message: String, location: Location): Nothing {
        System.err.println("$type error at $location: $message")
        exitProcess(1)
    }
}