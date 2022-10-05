package io.github.seggan.rol.tree.untyped

sealed class Var(val name: String) : Node(listOf())

class VarDef(name: String, val constant: Boolean, val type: Typename?) : Var(name) {
    override fun toString(): String {
        return "VarDef($name, constant=$constant, type=$type)"
    }
}

class VarAssign(name: String, val value: Expression) : Var(name) {
    override fun toString(): String {
        return "VarAssign($name, $value)"
    }
}

enum class AssignType(private val symbol: String, val operation: BinaryOperator?) {
    ASSIGN("=", null),
    PLUS_ASSIGN("+=", BinaryOperator.PLUS),
    MINUS_ASSIGN("-=", BinaryOperator.MINUS),
    TIMES_ASSIGN("*=", BinaryOperator.TIMES),
    DIVIDE_ASSIGN("/=", BinaryOperator.DIVIDE),
    MODULO_ASSIGN("%=", BinaryOperator.MODULO),
    BITWISE_AND_ASSIGN("&=", BinaryOperator.BITWISE_AND),
    BITWISE_OR_ASSIGN("|=", BinaryOperator.BITWISE_OR),
    BITWISE_XOR_ASSIGN("^=", BinaryOperator.XOR),
    BITWISE_LEFT_SHIFT_ASSIGN("<<=", BinaryOperator.SHIFT_LEFT),
    BITWISE_RIGHT_SHIFT_ASSIGN(">>=", BinaryOperator.SHIFT_RIGHT);

    override fun toString() = symbol

    companion object {
        fun fromSymbol(symbol: String): AssignType = values().first { it.symbol == symbol }
    }
}