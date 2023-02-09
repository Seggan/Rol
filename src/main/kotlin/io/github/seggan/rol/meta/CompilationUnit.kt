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
    fun parse(version: Int, data: D): T
}

data class FileUnit(
    override val simpleName: String,
    val pkg: String,
    val dependencies: Set<String>,
    val variables: Set<VariableUnit>,
    val classes: Set<ClassUnit>,
    val interfaces: Set<InterfaceUnit>,
    val text: String
) : CompilationUnit<String> {

    companion object : CompilationUnitParser<FileUnit?, Path> {
        override fun parse(version: Int, data: Path): FileUnit? {
            if (data.extension != "lua") return null
            return parse(data.readText())?.copy(simpleName = data.nameWithoutExtension)
        }

        fun parse(data: String): FileUnit? {
            val metaString = META_REGEX.find(data)?.groupValues?.get(1) ?: return null
            val meta = klaxon.parseJsonObject(metaString.reader())
            val ver = meta.int("version") ?: return null
            return when (ver) {
                1 -> parseV1(meta)
                else -> null
            }?.copy(text = data)
        }

        private fun parseV1(data: JsonObject): FileUnit {
            val pkg = data.string("package") ?: "unnamed"
            val dependencies = data.array<String>("dependencies")!!.toSet()
            val variables = data.array<JsonObject>("variables")!!.map { VariableUnit.parse(1, it) }.toSet()
            val classes = data.array<JsonObject>("classes")!!.map { ClassUnit.parse(1, it) }.toSet()
            val interfaces = data.array<JsonObject>("interfaces")!!.map { InterfaceUnit.parse(1, it) }.toSet()
            return FileUnit("", pkg, dependencies, variables, classes, interfaces, "")
        }
    }

    override fun serialize(): String {
        val obj = JsonObject(
            mapOf(
                "version" to CompilationUnit.VERSION,
                "package" to pkg,
                "dependencies" to dependencies.toList(),
                "variables" to variables.map(VariableUnit::serialize),
                "classes" to classes.map(ClassUnit::serialize),
                "interfaces" to interfaces.map(InterfaceUnit::serialize)
            )
        ).toJsonString()
        return "-- ROLMETA $obj\npackage.path = \"./?.lua;\" .. package.path\nrequire \"rol_core\"\n$text"
    }

    fun findClass(name: String): CompilationUnit<*>? {
        return classes.find { it.name.name == name } ?: interfaces.find { it.name.name == name }
    }

    fun findVariable(name: String): VariableUnit? {
        return variables.find { it.simpleName == name }
    }

    fun findSubunits(obj: String): List<CompilationUnit<*>> {
        return variables.filter { it.simpleName == obj } +
                classes.filter { it.name.name == obj } +
                interfaces.filter { it.name.name == obj }
    }
}

private val META_REGEX = "^\\s*-- ROLMETA (\\{.+})\\n".toRegex()
private val klaxon = Klaxon()
