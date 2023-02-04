package io.github.seggan.rol

import io.github.seggan.rol.antlr.RolLexer
import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.meta.FileUnit
import io.github.seggan.rol.parsing.ImportCollector
import io.github.seggan.rol.parsing.RolVisitor
import io.github.seggan.rol.parsing.TypeChecker
import io.github.seggan.rol.postype.ConstantFolder
import io.github.seggan.rol.postype.Transpiler
import io.github.seggan.rol.resolution.DependencyManager
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.untyped.UStatements
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.multiple
import kotlinx.cli.required
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSameFileAs
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.writeText
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val parser = ArgParser("rol")
    val file by parser.option(ArgType.String, shortName = "f", description = "File(s) to compile").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output directory")
    val include by parser.option(ArgType.String, shortName = "i", description = "Include files/directories").multiple()

    parser.parse(args)

    val theFile = Path.of(file)
    if (!theFile.exists()) {
        System.err.println("File $file does not exist")
        exitProcess(1)
    }
    val path = System.getenv("ROL_HOME")?.split(File.pathSeparatorChar)?.map(Path::of)?.toMutableList()
        ?: mutableListOf()
    path.add(theFile.parent)
    val includePaths = include.map(Path::of).filter(Files::exists)
    path.addAll(includePaths.filter(Files::isDirectory))

    val files = path.flatMap { p ->
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

    val compiledThis = theFile.resolveSibling("${theFile.nameWithoutExtension}.lua")

    var unit = compile(theFile, files + include.map(Path::of).filter {
        it.isRegularFile() && it.extension == "lua" && !it.isSameFileAs(compiledThis)
    })
    unit = unit.copy(text = DEPENDENCY_MANAGER.usedDependencies.joinToString("") {
        "require \"${it.simpleName}\"\n"
    } + unit.text)

    val outputName = output ?: theFile.nameWithoutExtension
    theFile.resolveSibling("$outputName.lua").writeText(unit.serialize())
    Files.newOutputStream(theFile.resolveSibling("rol_core.lua")).use { stream ->
        FileUnit::class.java.getResourceAsStream("/rol_core.lua")!!.use {
            it.copyTo(stream)
        }
    }
}

private lateinit var DEPENDENCY_MANAGER: DependencyManager
lateinit var CURRENT_FILE: String

fun compile(path: Path, files: List<Path>): FileUnit {
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

    val collector = ImportCollector()
    parsed.accept(collector)

    DEPENDENCY_MANAGER = DependencyManager(files, collector.explicitImports + ("rol" to setOf()))

    var typedAst: TNode = TypeChecker(DEPENDENCY_MANAGER, collector.imports, pkg).typeAst(ast)
    var folder: ConstantFolder
    do {
        folder = ConstantFolder()
        typedAst = folder.start(typedAst)
    } while (folder.changed)

    val transpiler = Transpiler(DEPENDENCY_MANAGER)
    val transpiledAst = transpiler.start(typedAst)
    return FileUnit(
        path.nameWithoutExtension,
        pkg,
        collector.imports + collector.explicitImports.keys,
        setOf(),
        setOf(),
        setOf(),
        transpiledAst.transpile()
    )
}