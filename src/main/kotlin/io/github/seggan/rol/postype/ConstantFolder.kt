package io.github.seggan.rol.postype

import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.typed.TypedTreeVisitor

object ConstantFolder : TypedTreeVisitor<TNode>() {



    override fun defaultValue(node: TNode): TNode {
        return node
    }
}