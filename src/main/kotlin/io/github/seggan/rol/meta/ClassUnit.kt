package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import io.github.seggan.rol.tree.common.AClass
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Type

class ClassUnit(
    override val name: Identifier,
    override val fields: Map<String, Type>,
    val superClass: Identifier
) : CompilationUnit<JsonObject>, AClass {

    override val simpleName = name.name

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
                Identifier.parseString(data.string("superClass")!!)
            )
        }
    }

    override fun serialize(): JsonObject {
        return JsonObject(
            mutableMapOf(
                "name" to name.toString(),
                "fields" to fields.map { (name, type) ->
                    JsonObject(
                        mapOf(
                            "name" to name,
                            "type" to type.toString()
                        )
                    )
                },
                "superClass" to superClass.toString()
            )
        )
    }
}

class InterfaceUnit(
    override val name: Identifier,
    override val fields: Map<String, Type>,
    val superInterfaces: List<Identifier> = emptyList()
) :
    CompilationUnit<JsonObject>, AClass {

    override val simpleName = name.name

    companion object : CompilationUnitParser<InterfaceUnit, JsonObject> {
        override fun parse(version: Int, data: JsonObject): InterfaceUnit {
            when (version) {
                1 -> return parseV1(data)
                else -> throw IllegalArgumentException("Unknown version $version")
            }
        }

        private fun parseV1(data: JsonObject): InterfaceUnit {
            val fields = data.array<JsonObject>("fields")!!.associate {
                it.string("name")!! to Type.parse(it.string("type")!!)
            }
            val superInterfaces = data.array<JsonObject>("superInterfaces")?.map {
                Identifier.parseString(it.string("name")!!)
            } ?: emptyList()
            return InterfaceUnit(
                Identifier.parseString(data.string("name")!!),
                fields,
                superInterfaces
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
                "superInterfaces" to superInterfaces.map { JsonObject(mapOf("name" to it.toString())) }
            )
        )
    }
}