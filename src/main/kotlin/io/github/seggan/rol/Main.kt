package io.github.seggan.rol

import io.github.seggan.rol.antlr.RolLexer
import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.meta.FileUnit
import io.github.seggan.rol.parsing.RolVisitor
import io.github.seggan.rol.parsing.TypeChecker
import io.github.seggan.rol.postype.ConstantFolder
import io.github.seggan.rol.postype.Transpiler
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.untyped.UStatements
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
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

fun main() {
    val file = Path.of("rolbuild/test.rol")
    val path = System.getenv("ROL_HOME")?.split(File.pathSeparatorChar)?.map(Path::of) ?: emptyList()
    val files = path.flatMap { p ->
        val files = mutableListOf<Path>()
        Files.walkFileTree(p, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.extension == "rol" || file.extension == "lua") {
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
    DEPENDENCY_MANAGER = DependencyManager(files)
    val unit = getCompilationUnit(file) ?: return
    Files.writeString(file.resolveSibling("${file.nameWithoutExtension}.lua"), unit.serialize())
    Files.newOutputStream(file.resolveSibling("rol_core.lua")).use { stream ->
        FileUnit::class.java.getResourceAsStream("/rol_core.lua")!!.use {
            it.copyTo(stream)
        }
    }
}

private lateinit var DEPENDENCY_MANAGER: DependencyManager

fun compile(path: Path): FileUnit {
    // temporarily replace STDERR to catch ANTLR errors
    val stderr = System.err
    val newErr = ErrCatcher()
    System.setErr(PrintStream(newErr))

    val stream = CommonTokenStream(RolLexer(CharStreams.fromPath(path, StandardCharsets.UTF_8)))
    val parsed = RolParser(stream).file()

    System.setErr(stderr)
    newErr.buffer.forEach(System.err::write)
    if (newErr.buffer.isNotEmpty()) {
        exitProcess(1)
    }

    val pkg = parsed.packageStatement()?.package_()?.text ?: "unnamed"
    val imports = parsed.package_().map(ParserRuleContext::getText).union(
        setOfNotNull(
            if (pkg == "rol.lang") null else "rol.lang"
        )
    )

    val ast = parsed.accept(RolVisitor()) as UStatements
    println(ast)
    var typedAst: TNode = TypeChecker(DEPENDENCY_MANAGER, imports).typeAst(ast)
    var folder: ConstantFolder
    do {
        folder = ConstantFolder()
        typedAst = folder.start(typedAst)
    } while (folder.changed)
    val transpiledAst = Transpiler(DEPENDENCY_MANAGER, imports).start(typedAst)
    return FileUnit(path.nameWithoutExtension, pkg, setOf(), setOf(), transpiledAst.transpile())
}

private class ErrCatcher : OutputStream() {

    val buffer = mutableListOf<Int>()

    override fun write(b: Int) {
        buffer.add(b)
    }
}