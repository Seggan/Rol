package io.github.seggan.rol.postype

import io.github.seggan.rol.tree.typed.TNode
import io.github.seggan.rol.tree.typed.TypedTreeVisitor

abstract class NodeCollector<T : TNode> : TypedTreeVisitor<Unit>() {

    private val nodes = mutableListOf<T>()

    fun collect(node: TNode): List<T> {
        nodes.clear()
        start(node)
        return nodes
    }

    protected fun add(node: T) {
        nodes.add(node)
    }

    override fun defaultValue(node: TNode) {
        return
    }
}