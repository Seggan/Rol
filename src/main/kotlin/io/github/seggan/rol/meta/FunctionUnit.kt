package io.github.seggan.rol.meta

import com.beust.klaxon.JsonObject
import io.github.seggan.rol.tree.common.Type
import io.github.seggan.rol.tree.typed.TExpression
import io.github.seggan.rol.tree.untyped.UFunctionCall

data class FunctionUnit(val name: String, val mangled: String, val args: List<ArgUnit>, val returnType: Type) :
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
            val args = data.array<JsonObject>("args")!!.map { ArgUnit.parse(1, it) }
            val returnType = Type.parse(data.string("returnType")!!)
            return FunctionUnit(name, mangled, args, returnType)
        }
    }

    override fun serialize(): JsonObject {
        return JsonObject(
            mapOf(
                "name" to name,
                "mangled" to mangled,
                "args" to args.map(ArgUnit::serialize),
                "returnType" to returnType.toString()
            )
        )
    }

    fun matches(fn: UFunctionCall, oargs: List<TExpression>): Boolean {
        if (fn.name != name) return false
        if (args.size != oargs.size) return false
        for (i in args.indices) {
            if (!args[i].type.isAssignableFrom(oargs[i].type)) return false
        }
        return true
    }
}

data class ArgUnit(val name: String, val type: Type) : CompilationUnit<JsonObject> {
    companion object : CompilationUnitParser<ArgUnit, JsonObject> {
        override fun parse(version: Int, data: JsonObject): ArgUnit {
            return when (version) {
                1 -> parseV1(data)
                else -> throw IllegalArgumentException("Unknown version $version")
            }
        }

        private fun parseV1(data: JsonObject): ArgUnit {
            val name = data.string("name")!!
            val type = Type.parse(data.string("type")!!)
            return ArgUnit(name, type)
        }
    }

    override fun serialize(): JsonObject {
        return JsonObject(
            mapOf(
                "name" to name,
                "type" to type.toString()
            )
        )
    }
}