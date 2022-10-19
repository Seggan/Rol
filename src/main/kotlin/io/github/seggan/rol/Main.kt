package io.github.seggan.rol

import io.github.seggan.rol.antlr.RolLexer
import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.meta.ArgUnit
import io.github.seggan.rol.meta.FileUnit
import io.github.seggan.rol.meta.FunctionUnit
import io.github.seggan.rol.parsing.RolVisitor
import io.github.seggan.rol.parsing.TypeChecker
import io.github.seggan.rol.postype.ConstantFolder
import io.github.seggan.rol.postype.Transpiler
import io.github.seggan.rol.tree.typed.TFunctionDeclaration
import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.untyped.UStatements
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.multiple
import kotlinx.cli.required
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

    val extensions = setOf("rol", "lua")
    val files = path.flatMap { p ->
        val files = mutableListOf<Path>()
        Files.walkFileTree(p, object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                if (file.extension in extensions) {
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

    DEPENDENCY_MANAGER = DependencyManager(files + include.map(Path::of).filter {
        it.isRegularFile() && it.extension in extensions && !it.isSameFileAs(compiledThis)
    })

    var unit = getCompilationUnit(theFile) ?: return
    unit = unit.copy(text = DEPENDENCY_MANAGER.usedDependencies.joinToString {
        "require \"${it.name}\"\n"
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
    var typedAst: TNode = TypeChecker(DEPENDENCY_MANAGER, imports).typeAst(ast)
    var folder: ConstantFolder
    do {
        folder = ConstantFolder()
        typedAst = folder.start(typedAst)
    } while (folder.changed)

    val transpiler = Transpiler(DEPENDENCY_MANAGER, imports)
    val transpiledAst = transpiler.start(typedAst)
    val functions =
        transpiler.functions.filterKeys { it is TFunctionDeclaration }.mapKeys { it.key as TFunctionDeclaration }
    return FileUnit(
        path.nameWithoutExtension,
        pkg,
        functions.map {
            FunctionUnit(it.key.name, it.value, it.key.args.map { a ->
                ArgUnit(a.name, a.type)
            }, it.key.type)
        }.toSet(),
        setOf(),
        transpiledAst.transpile()
    )
}

private class ErrCatcher : OutputStream() {

    val buffer = mutableListOf<Int>()

    override fun write(b: Int) {
        buffer.add(b)
    }
}