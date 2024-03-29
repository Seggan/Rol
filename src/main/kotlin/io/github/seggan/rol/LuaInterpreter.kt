package io.github.seggan.rol

import org.luaj.vm2.LuaString
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.jse.JsePlatform
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.nameWithoutExtension

class RolInterpreter(val code: String, val paths: List<Path>) {

    private val globals = JsePlatform.standardGlobals()
    private val defaultRequire = globals.get("require")

    init {
        globals.set("require", CustomRequire())
    }

    fun run() {
        globals.load(code).call()
    }

    inner class CustomRequire : OneArgFunction() {
        override fun call(arg: LuaValue): LuaValue {
            if (arg is LuaString) {
                val name = arg.tojstring()
                if (name == "rol_core") {
                    val core = "rol_core.lua"
                    return globals.load(getResource(core), core, "t", globals).call()
                } else {
                    val stdStream = getResource("stdlib/$name.lua")
                    if (stdStream != null) {
                        return globals.load(stdStream, "$name.lua", "t", globals).call()
                    } else {
                        for (path in paths) {
                            if (path.nameWithoutExtension == name && path.exists()) {
                                return globals.loadfile(path.toAbsolutePath().toString()).call()
                            }
                        }
                    }
                }
            }
            return defaultRequire.call(arg)
        }
    }
}