package io.github.seggan.rol.resolution

import io.github.seggan.rol.tree.common.Argument
import io.github.seggan.rol.tree.common.Type

data class FunctionHeader(val name: String, val args: List<Argument>, val returnType: Type) {

    fun matches(name: String, args: List<Type>): Boolean {
        return this.name == name && this.args.size == args.size && this.args.zip(args).all { (targ, arg) ->
            targ.type.isAssignableFrom(arg)
        }
    }
}