package com.golfing8.kcommon.idea.preview

/**
 * Renders KCommon text (legacy `&` codes, `&#rrggbb` hex colors and the common MiniMessage tags)
 * as HTML spans, the way HytaleMessages turns it into a Hytale message through Adventure. Other
 * MiniMessage tags are dropped; a closing tag resets to the base style, and a gradient uses its
 * first color.
 */
object TextFormat {
    private val NAMED = linkedMapOf(
        "black" to "#000000", "dark_blue" to "#0000aa", "dark_green" to "#00aa00", "dark_aqua" to "#00aaaa",
        "dark_red" to "#aa0000", "dark_purple" to "#aa00aa", "gold" to "#ffaa00", "gray" to "#aaaaaa",
        "dark_gray" to "#555555", "blue" to "#5555ff", "green" to "#55ff55", "aqua" to "#55ffff",
        "red" to "#ff5555", "light_purple" to "#ff55ff", "yellow" to "#ffff55", "white" to "#ffffff",
    )
    private val LEGACY: Map<Char, String> = "0123456789abcdef".toList().zip(NAMED.values.toList()).toMap()
    private val HEX = Regex("#[0-9a-fA-F]{6}")

    private data class Style(
        val color: String,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val underline: Boolean = false,
        val strike: Boolean = false,
    )

    fun toHtml(text: String, baseColor: String): String {
        val base = Style(baseColor)
        var style = base
        val out = StringBuilder()
        val run = StringBuilder()

        fun flush() {
            if (run.isEmpty()) return
            out.append("<span style=\"color:").append(style.color)
            if (style.bold) out.append(";font-weight:700")
            if (style.italic) out.append(";font-style:italic")
            val decoration = listOfNotNull("underline".takeIf { style.underline }, "line-through".takeIf { style.strike })
            if (decoration.isNotEmpty()) out.append(";text-decoration:").append(decoration.joinToString(" "))
            out.append("\">").append(HytaleMenuHtml.escape(run.toString()).replace("\n", "<br>")).append("</span>")
            run.setLength(0)
        }

        var i = 0
        while (i < text.length) {
            val c = text[i]
            if ((c == '&' || c == '§') && i + 1 < text.length) {
                val code = text[i + 1].lowercaseChar()
                if (code == '#' && i + 8 <= text.length && HEX.matches(text.substring(i + 1, i + 8))) {
                    flush(); style = Style(text.substring(i + 1, i + 8)); i += 8; continue
                }
                val next = when (code) {
                    in LEGACY -> Style(LEGACY.getValue(code))
                    'l' -> style.copy(bold = true)
                    'o' -> style.copy(italic = true)
                    'n' -> style.copy(underline = true)
                    'm' -> style.copy(strike = true)
                    'k' -> style
                    'r' -> base
                    else -> null
                }
                if (next != null) {
                    flush(); style = next; i += 2; continue
                }
            }
            if (c == '<') {
                val close = text.indexOf('>', i)
                if (close > i) {
                    val next = tag(text.substring(i + 1, close).trim().lowercase(), style, base)
                    if (next != null) {
                        flush(); style = next; i = close + 1; continue
                    }
                }
            }
            run.append(c)
            i++
        }
        flush()
        return out.toString()
    }

    /** The style after a MiniMessage tag, or null when it isn't a tag this understands (it's then shown as text). */
    private fun tag(tag: String, style: Style, base: Style): Style? {
        if (tag.startsWith("/")) return base
        val name = tag.substringBefore(':')
        val arg = tag.substringAfter(':', "")
        return when {
            HEX.matches(tag) -> Style(tag)
            name in NAMED -> Style(NAMED.getValue(name))
            name == "color" || name == "c" || name == "colour" -> (NAMED[arg] ?: arg.takeIf(HEX::matches))?.let { Style(it) }
            name == "gradient" || name == "transition" -> arg.split(':').firstNotNullOfOrNull { NAMED[it] ?: it.takeIf(HEX::matches) }?.let { Style(it) }
            name == "b" || name == "bold" -> style.copy(bold = true)
            name == "i" || name == "em" || name == "italic" -> style.copy(italic = true)
            name == "u" || name == "underlined" -> style.copy(underline = true)
            name == "st" || name == "strikethrough" -> style.copy(strike = true)
            name == "reset" -> base
            name in setOf("rainbow", "hover", "click", "font", "insert", "key", "lang", "obf", "obfuscated", "newline", "br") -> style
            else -> null
        }
    }
}
