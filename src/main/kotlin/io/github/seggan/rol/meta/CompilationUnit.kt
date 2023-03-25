package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText

sealed interface CompilationUnit<D> {
    companion object {
        const val VERSION = 1
    }

    val simpleName: String

    fun serialize(): D
}

sealed interface CompilationUnitParser<T, D> {
    fun parse(data: D): T
}

data class FileUnit(
    override val simpleName: String,
    val pkg: String,
    val dependencies: Set<String>,
    val variables: Set<VariableUnit>,
    val structs: Set<StructUnit>,
    val traits: Set<TraitUnit>,
    val text: String
) : CompilationUnit<String> {

    companion object : CompilationUnitParser<FileUnit?, Path> {
        override fun parse(data: Path): FileUnit? {
            if (data.extension != "lua") return null
            return parse(data.readText())?.copy(simpleName = data.nameWithoutExtension)
        }

        fun parse(data: String): FileUnit? {
            val metaString = META_REGEX.find(data)?.groupValues?.get(1) ?: return null
            val meta = klaxon.parseJsonObject(metaString.reader())
            val pkg = meta.string("package") ?: "unnamed"
            val dependencies = meta.array<String>("dependencies")!!.toSet()
            val variables = meta.array<JsonObject>("variables")!!.map { VariableUnit.parse(it) }.toSet()
            val classes = meta.array<JsonObject>("classes")!!.map { StructUnit.parse(it) }.toSet()
            val interfaces = meta.array<JsonObject>("interfaces")!!.map { TraitUnit.parse(it) }.toSet()
            return FileUnit("", pkg, dependencies, variables, classes, interfaces, data)
        }

    }

    override fun serialize(): String {
        val obj = JsonObject(
            mapOf(
                "version" to CompilationUnit.VERSION,
                "package" to pkg,
                "dependencies" to dependencies.toList(),
                "variables" to variables.map(VariableUnit::serialize),
                "classes" to structs.map(StructUnit::serialize),
                "interfaces" to traits.map(TraitUnit::serialize)
            )
        ).toJsonString()
        return "-- ROLMETA $obj\npackage.path = \"./?.lua;\" .. package.path\nrequire \"rol_core\"\n$text"
    }

    fun findClass(name: String): CompilationUnit<*>? {
        return structs.find { it.name.name == name } ?: traits.find { it.name.name == name }
    }

    fun findVariable(name: String): VariableUnit? {
        return variables.find { it.simpleName == name }
    }

    fun findSubunits(obj: String): List<CompilationUnit<*>> {
        return variables.filter { it.simpleName == obj } +
                structs.filter { it.name.name == obj } +
                traits.filter { it.name.name == obj }
    }
}

private val META_REGEX = """^\s*-- ROLMETA (\{.+})\r?\n""".toRegex()
private val klaxon = Klaxon()
