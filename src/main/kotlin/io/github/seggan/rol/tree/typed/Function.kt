package io.github.seggan.rol.tree.typed

import io.github.seggan.rol.tree.AccessModifier
import io.github.seggan.rol.tree.Location

sealed class TFn(val name: String, val args: List<TArgument>, type: Type, children: List<TNode>, location: Location) :
    TNode(type, children, location)

class TFunctionDeclaration(
    name: String,
    args: List<TArgument>,
    type: Type,
    val access: AccessModifier,
    val body: TStatements,
    location: Location
) : TFn(name, args, type, listOf(body), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitFunctionDeclaration(this)
    }
}

class TExternDeclaration(name: String, val nativeName: String, args: List<TArgument>, location: Location) :
    TFn(name, args, Type.DYNAMIC, listOf(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitExternDeclaration(this)
    }
}

class TArgument(name: String, type: Type, location: Location) : TVar(name, type, listOf(), location) {
    override fun <T> accept(visitor: TypedTreeVisitor<T>): T {
        return visitor.visitArgument(this)
    }
}