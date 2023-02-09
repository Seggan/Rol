package io.github.seggan.rol

import io.github.seggan.rol.antlr.RolLexer
import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.meta.FileUnit
import io.github.seggan.rol.meta.VariableUnit
import io.github.seggan.rol.parsing.ImportCollector
import io.github.seggan.rol.parsing.RolVisitor
import io.github.seggan.rol.parsing.TypeChecker
import io.github.seggan.rol.postype.ConstantFolder
import io.github.seggan.rol.postype.Transpiler
import io.github.seggan.rol.postype.mangleIdentifier
import io.github.seggan.rol.resolution.DependencyManager
import io.github.seggan.rol.resolution.TypeResolver
import io.github.seggan.rol.tree.common.AccessModifier
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.typed.TVarDef
import io.github.seggan.rol.tree.untyped.UStatements
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.system.exitProcess


lateinit var CURRENT_FILE: String

internal fun getFiles(path: List<Path>): List<Path> {
    return path.flatMap { p ->
        val files = mutableListOf<Path>()
        Files.walkFileTree(p, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.extension == "lua") {
                    files.add(file)
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                return FileVisitResult.CONTINUE // ignore
            }
        })
        files
    }
}

fun compile(path: Path, files: List<Path>): Pair<FileUnit, DependencyManager> {
    // temporarily replace STDERR to catch ANTLR errors
    val stderr = System.err
    val newErr = ByteArrayOutputStream()
    System.setErr(PrintStream(newErr))

    val stream = CommonTokenStream(RolLexer(CharStreams.fromPath(path, StandardCharsets.UTF_8)))
    val parsed = RolParser(stream).file()

    CURRENT_FILE = path.fileName.toString()

    System.err.flush()
    System.setErr(stderr)
    if (newErr.size() > 0) {
        newErr.writeTo(System.err)
        exitProcess(1)
    }

    val pkg = parsed.packageStatement()?.package_()?.text ?: "unnamed"

    val ast = parsed.accept(RolVisitor()) as UStatements
    println(ast)

    val collector = ImportCollector()
    parsed.accept(collector)

    val explicitImports = collector.explicitImports + ("rol" to setOf())
    val dependencyManager = DependencyManager(files, explicitImports)

    val resolver = TypeResolver(dependencyManager, pkg, collector.imports + explicitImports.keys)
    var typedAst: TNode = TypeChecker(resolver, pkg).typeAst(ast)
    var folder: ConstantFolder
    do {
        folder = ConstantFolder()
        typedAst = folder.start(typedAst)
    } while (folder.changed)

    val transpiler = Transpiler(resolver)
    val transpiledAst = transpiler.start(typedAst)
    return FileUnit(
        path.nameWithoutExtension,
        pkg,
        collector.imports + collector.explicitImports.keys,
        typedAst.children
            .filterIsInstance<TVarDef>()
            .filter { it.modifiers.access == AccessModifier.PUBLIC }
            .mapTo(mutableSetOf()) {
                VariableUnit(
                    it.name.name,
                    mangleIdentifier(it.name, it.type),
                    it.type
                )
            },
        setOf(),
        setOf(),
        transpiledAst.transpile()
    ) to dependencyManager
}