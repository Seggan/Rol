package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import io.github.seggan.rol.tree.common.Type

data class FunctionUnit(val name: String, val mangled: String, val args: LinkedHashMap<String, Type>, val returnType: Type) :
    CompilationUnit<JsonObject> {
    companion object : CompilationUnitParser<FunctionUnit, JsonObject> {
        override fun parse(version: Int, data: JsonObject): FunctionUnit {
            return when (version) {
                1 -> parseV1(data)
                else -> throw IllegalArgumentException("Unknown version $version")
            }
        }

        private fun parseV1(data: JsonObject): FunctionUnit {
            val name = data.string("name")!!
            val mangled = data.string("mangled")!!
            val args = data.array<JsonObject>("args")!!.associateTo(LinkedHashMap()) {
                it.string("name")!! to Type.parse(it.string("type")!!)
            }
            val returnType = Type.parse(data.string("returnType")!!)
            return FunctionUnit(name, mangled, args, returnType)
        }
    }

    override fun serialize(): JsonObject {
        return JsonObject(
            mapOf(
                "name" to name,
                "mangled" to mangled,
                "args" to args.map {
                    JsonObject(
                        mapOf(
                            "name" to it.key,
                            "type" to it.value.toString()
                        )
                    )
                },
                "returnType" to returnType.toString()
            )
        )
    }
}