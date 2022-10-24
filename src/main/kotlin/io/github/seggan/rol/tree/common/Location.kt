package io.github.seggan.rol.tree.common

import io.github.seggan.rol.CURRENT_FILE
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval

data class Location(val row: Int, val column: Int, val file: String? = null, val text: String? = null) {
    override fun toString(): String {
        return buildString {
            if (file != null) {
                append("file '")
                append(file)
                append("', ")
            }
            append("line $row, column $column")
            if (text != null) {
                append(", statement '")
                append(text)
                append('\'')
            }
        }
    }
}

val ParserRuleContext.location: Location
    get() = Location(
        start.line,
        start.charPositionInLine,
        CURRENT_FILE,
        start.inputStream.getText(Interval(start.startIndex, stop.stopIndex))
    )
