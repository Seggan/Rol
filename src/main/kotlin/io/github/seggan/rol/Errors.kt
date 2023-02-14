package io.github.seggan.rol

import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Type
import mu.KotlinLogging
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

object Errors {

    fun classDefinition(name: Identifier, message: String, location: Location): Nothing {
        genericError("Class definition", "For class $name: $message", location)
    }

    fun undefinedReference(name: String, location: Location): Nothing {
        genericError("Unknown reference", "$name is not defined", location)
    }

    fun duplicateDefinition(name: String, location: Location): Nothing {
        genericError("Duplicate definition", "$name is already defined", location)
    }

    fun typeMismatch(expected: Type, actual: Type, location: Location): Nothing {
        genericError("Type mismatch", "Expected $expected, got $actual", location)
    }

    fun typeError(message: String, location: Location): Nothing {
        genericError("Type", message, location)
    }

    fun genericError(type: String, message: String, location: Location): Nothing {
        val msg = "$type error at $location: $message"
        System.err.println(msg)
        val stringWriter = StringWriter()
        val printWriter = PrintWriter(stringWriter)
        Thread.currentThread().stackTrace.drop(1).forEach { printWriter.println(it) }
        logger.debug {
            msg + "\n" + stringWriter.toString().trim().prependIndent("\tat ")
        }
        exitProcess(1)
    }
}