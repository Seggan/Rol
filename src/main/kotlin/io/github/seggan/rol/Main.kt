package io.github.seggan.rol

import io.github.seggan.rol.antlr.RolLexer
import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.parsing.RolVisitor
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.nio.charset.StandardCharsets
import java.nio.file.Path

fun main() {
    val file = Path.of("test.rol")
    val stream = CommonTokenStream(RolLexer(CharStreams.fromPath(file, StandardCharsets.UTF_8)))
    val parsed = RolParser(stream).file()
    val ast = parsed.accept(RolVisitor())
    println(ast)
}