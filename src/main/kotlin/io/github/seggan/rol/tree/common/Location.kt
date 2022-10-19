package io.github.seggan.rol.tree.common

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval

data class Location(val row: Int, val column: Int, val text: String? = null) {
    override fun toString() = "row $row, column $column${if (text != null) ", statement '$text'" else ""}"
}

val ParserRuleContext.location: Location
    get() = Location(start.line, start.charPositionInLine, start.inputStream.getText(Interval(start.startIndex, stop.stopIndex)))
