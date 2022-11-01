package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.common.Argument
import io.github.seggan.rol.tree.common.Identifier
import io.github.seggan.rol.tree.common.Location
import io.github.seggan.rol.tree.common.Modifiers
import io.github.seggan.rol.tree.common.Type

sealed class TFn(
    val name: Identifier,
    val args: List<Argument>,
    type: Type,
    val modifiers: Modifiers,
    children: List<TNode>,
    location: Location
) :
    TNode(type, children, location) {

    fun matches(name: String, args: List<TNode>): Boolean {
        return this.name.name == name && this.args.size == args.size && this.args.zip(args).all { (targ, arg) ->
            targ.type.isAssignableFrom(arg.type)
        }
    }

    fun matches(name: Identifier, args: List<TNode>): Boolean {
        return this.name.pkg == name.pkg && matches(name.name, args)
    }
}

class TFunctionDeclaration(
    name: Identifier,
    args: List<Argument>,
    type: Type,
    modifiers: Modifiers,
    val body: TStatements,
    location: Location
) : TFn(name, args, type, modifiers, listOf(body), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitFunctionDeclaration(this)
    }
}

class TExternDeclaration(
    name: Identifier,
    args: List<Argument>,
    modifiers: Modifiers,
    val body: String,
    type: Type,
    location: Location
) :
    TFn(name, args, type, modifiers, listOf(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitExternDeclaration(this)
    }
}

class TReturn(val value: TExpression?, location: Location) :
    TNode(value?.type ?: Type.VOID, listOfNotNull(value), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitReturn(this)
    }
}