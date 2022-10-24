package io.github.seggan.rol.tree.lua

class LIfStatement(
    private val condition: LExpression,
    private val body: LStatements,
    private val elseBody: LStatements?
) : LNode() {
    override fun transpile(): String {
        val transpiled = "if ${condition.transpile()} then\n${body.transpile()}\n"
        return if (elseBody != null) {
            transpiled + "else\n${elseBody.transpile()}\nend"
        } else {
            transpiled + "end"
        }
    }
}