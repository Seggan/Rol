package io.github.seggan.rol

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.split
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSameFileAs
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.writeText

fun main(args: Array<String>) = ArgParser().main(args)

private class ArgParser : CliktCommand() {

    val file by argument(help = "File to compile").file(mustExist = true, canBeDir = false, mustBeReadable = true)
    val output by option("-o", "--output", help = "Output file name")
        .file(mustExist = false, canBeDir = false, mustBeWritable = false)
    val include by option("-I", "--include", help = "Include files/directories")
        .file(mustExist = true, canBeDir = true, mustBeReadable = true)
        .split("""\s+""".toRegex())
        .default(emptyList())
    val interpret by option("-i", "--interpret", help = "Interpret instead of compiling")
        .flag("-c", "--compile")

    override fun run() {
        val theFile = file.toPath()
        val path = System.getenv("ROL_HOME")?.split(File.pathSeparatorChar)?.map(Path::of)?.toMutableList()
            ?: mutableListOf()
        path.add(theFile.parent)
        val includePaths = include.map(File::toPath).filter(Files::exists)
        path.addAll(includePaths.filter(Files::isDirectory))

        val compiledThis = theFile.resolveSibling("${theFile.nameWithoutExtension}.lua")

        var (unit, dependencyManager) = compile(theFile, getFiles(path) + include.map(File::toPath).filter {
            it.isRegularFile() && it.extension == "lua" && !it.isSameFileAs(compiledThis)
        })
        unit = unit.copy(text = dependencyManager.usedDependencies.joinToString("") {
            "require \"${it.simpleName}\"\n"
        } + unit.text)

        if (interpret) {
            RolInterpreter(unit.serialize(), path).run()
        } else {
            val outputFile = output?.toPath() ?: theFile.resolveSibling("${theFile.nameWithoutExtension}.lua")
            outputFile.writeText(unit.serialize())
        }
    }
}

fun getResource(name: String): InputStream? {
    return ArgParser::class.java.getResourceAsStream("/$name")
}