package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.common.toType

data class VariableUnit(override val simpleName: String, val mangled: String, val type: Type) : CompilationUnit<JsonObject> {
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
                data.string("type")!!.toType()
            )
        }
    }

    override fun serialize(): JsonObject {
        return JsonObject(
            mapOf(
                "name" to simpleName,
                "mangled" to mangled,
                "type" to type.toString()
            )
        )
    }
}
