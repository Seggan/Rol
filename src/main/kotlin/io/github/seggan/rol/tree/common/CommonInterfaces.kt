package io.github.seggan.rol.tree.common

interface Reference {
    val name: String
}

interface AClass {
    val name: Identifier
    val fields: Map<String, Type>
    val methods: Map<String, AFunction>
}

interface AFunction {
    val name: String
    val parameters: List<Type>
    val returnType: Type
}