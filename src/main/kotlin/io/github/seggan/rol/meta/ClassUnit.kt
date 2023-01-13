package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Struct
import io.github.seggan.rol.tree.common.Type

class ClassUnit(
    override val name: Identifier,
    override val fields: Map<String, Type>,
    override val const: Boolean
) :
    CompilationUnit<JsonObject>, Struct {

    companion object : CompilationUnitParser<ClassUnit, JsonObject> {
        override fun parse(version: Int, data: JsonObject): ClassUnit {
            when (version) {
                1 -> return parseV1(data)
                else -> throw IllegalArgumentException("Unknown version $version")
            }
        }

        private fun parseV1(data: JsonObject): ClassUnit {
            val fields = data.array<JsonObject>("fields")!!.associate {
                it.string("name")!! to Type.parse(it.string("type")!!)
            }
            return ClassUnit(
                Identifier.parseString(data.string("name")!!),
                fields,
                data.boolean("const")!!
            )
        }
    }

    override fun serialize(): JsonObject {
        return JsonObject(
            mapOf(
                "name" to name.toString(),
                "fields" to fields.map { (name, type) ->
                    JsonObject(
                        mapOf(
                            "name" to name,
                            "type" to type.toString()
                        )
                    )
                },
                "const" to const
            )
        )
    }
}