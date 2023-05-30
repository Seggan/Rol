package io.github.seggan.rol.parsing

import io.github.seggan.rol.antlr.RolParser
import io.github.seggan.rol.antlr.RolParserBaseVisitor

class ImportCollector : RolParserBaseVisitor<Unit>() {

    val imports = mutableSetOf<String>()
    val explicitImports = mutableMapOf<String, MutableSet<String>>()

    override fun visitUsingStatement(ctx: RolParser.UsingStatementContext) {
        imports.add(ctx.package_().text)
    }

    override fun visitUsingInStatement(ctx: RolParser.UsingInStatementContext) {
        val pkg = ctx.package_().text
        val name = ctx.id().map { it.text }
        explicitImports.getOrPut(pkg) { mutableSetOf() }.addAll(name)
    }

    override fun visitStatements(ctx: RolParser.StatementsContext) {
        return
    }
}