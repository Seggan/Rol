package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import io.github.seggan.rol.tree.common.Type

data class VariableUnit(val name: String, val mangled: String, val type: Type) : CompilationUnit<JsonObject> {
    companion object : CompilationUnitParser<VariableUnit, JsonObject> {
        override fun parse(version: Int, data: JsonObject): VariableUnit {
            return when (version) {
                1 -> parseV1(data)
                else -> throw IllegalArgumentException("Unknown version $version")
            }
        }

        private fun parseV1(data: JsonObject): VariableUnit {
            return VariableUnit(
                data.string("name")!!,
                data.string("mangled")!!,
                Type.parse(data.string("type")!!)
            )
        }
    }

    override fun serialize(): JsonObject {
        return JsonObject(
            mapOf(
                "name" to name,
                "mangled" to mangled,
                "type" to type.toString()
            )
        )
    }
}
