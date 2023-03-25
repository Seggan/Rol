package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import io.github.seggan.rol.tree.common.FunctionType
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.common.toType

class StructUnit(
    val name: Identifier,
    val fields: Map<String, Type>
) : CompilationUnit<JsonObject> {

    override val simpleName = name.name

    companion object : CompilationUnitParser<StructUnit, JsonObject> {
        override fun parse(data: JsonObject): StructUnit {
            val fields = data.array<JsonObject>("fields")!!.associate {
                it.string("name")!! to it.string("type")!!.toType()
            }
            return StructUnit(
                Identifier.parseString(data.string("name")!!),
                fields
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
                }
            )
        )
    }
}

class TraitUnit(
    val name: Identifier,
    val funs: Map<String, FunctionType>,
    val superTraits: List<Identifier> = emptyList()
) : CompilationUnit<JsonObject> {

    override val simpleName = name.name

    companion object : CompilationUnitParser<TraitUnit, JsonObject> {
        override fun parse(data: JsonObject): TraitUnit {
            val funs = data.array<JsonObject>("fields")!!.associate {
                it.string("name")!! to it.string("type")!!.toType() as FunctionType
            }
            val superTraits = data.array<JsonObject>("superTraits")?.map {
                Identifier.parseString(it.string("name")!!)
            } ?: emptyList()
            return TraitUnit(
                Identifier.parseString(data.string("name")!!),
                funs,
                superTraits
            )
        }
    }

    override fun serialize(): JsonObject {
        return JsonObject(
            mapOf(
                "name" to name.toString(),
                "fields" to funs.map { (name, type) ->
                    JsonObject(
                        mapOf(
                            "name" to name,
                            "type" to type.toString()
                        )
                    )
                },
                "superTraits" to superTraits.map { JsonObject(mapOf("name" to it.toString())) }
            )
        )
    }
}