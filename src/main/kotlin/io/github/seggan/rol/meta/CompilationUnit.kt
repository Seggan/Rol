package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import io.github.seggan.rol.tree.common.Type
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText

sealed interface CompilationUnit<D> {
    companion object {
        const val VERSION = 1
    }

    fun serialize(): D
}

sealed interface CompilationUnitParser<T, D> {
    fun parse(version: Int, data: D): T
}

data class FileUnit(
    val name: String,
    val pkg: String,
    val functions: Set<FunctionUnit>,
    val variables: Set<VariableUnit>,
    val structs: Set<StructUnit>,
    val text: String
) : CompilationUnit<String> {
    companion object : CompilationUnitParser<FileUnit?, Path> {
        override fun parse(version: Int, data: Path): FileUnit? {
            if (data.extension != "lua") return null
            val code = data.readText(StandardCharsets.UTF_8)
            val metaString = META_REGEX.find(code)?.groupValues?.get(1) ?: return null
            val meta = klaxon.parseJsonObject(metaString.reader())
            val ver = meta.int("version") ?: return null
            return when (ver) {
                1 -> parseV1(meta)
                else -> null
            }?.copy(name = data.nameWithoutExtension, text = code)
        }

        private fun parseV1(data: JsonObject): FileUnit {
            val pkg = data.string("package") ?: "unnamed"
            val functions = data.array<JsonObject>("functions")!!.map { FunctionUnit.parse(1, it) }.toSet()
            val variables = data.array<JsonObject>("variables")!!.map { VariableUnit.parse(1, it) }.toSet()
            val structs = data.array<JsonObject>("structs")!!.map { StructUnit.parse(1, it) }.toSet()
            return FileUnit("", pkg, functions, variables, structs, "")
        }
    }

    override fun serialize(): String {
        val obj = JsonObject(
            mapOf(
                "version" to CompilationUnit.VERSION,
                "package" to pkg,
                "functions" to functions.map(FunctionUnit::serialize),
                "variables" to variables.map(VariableUnit::serialize),
                "structs" to structs.map(StructUnit::serialize)
            )
        ).toJsonString()
        return "-- ROLMETA $obj\npackage.path = \"./?.lua;\" .. package.path\nrequire \"rol_core\"\n$text"
    }

    fun findFunction(name: String, args: List<Type>): FunctionUnit? {
        return functions.find {
            it.name == name && it.args.size == args.size && it.args.values.zip(args)
                .all { (a, b) -> a.isAssignableFrom(b) }
        }
    }

    fun findStruct(name: String): StructUnit? {
        return structs.find { it.name.name == name }
    }
}

private val META_REGEX = "^\\s*-- ROLMETA (\\{.+})\\n".toRegex()
private val klaxon = Klaxon()
