package com.golfing8.kcommon.idea

/**
 * A minimal, dependency-free JSON reader/writer used only for this plugin's schema-export format
 * (see [SchemaExport]) - deliberately not a general-purpose JSON library. Supports objects, arrays,
 * strings and booleans (no numbers - nothing in [ConfigFieldType] needs one).
 */
sealed class JsonValue {
    class JsonObject(val entries: LinkedHashMap<String, JsonValue> = LinkedHashMap()) : JsonValue() {
        operator fun get(key: String): JsonValue? = entries[key]
        operator fun set(key: String, value: JsonValue) {
            entries[key] = value
        }
    }

    class JsonArray(val items: MutableList<JsonValue> = mutableListOf()) : JsonValue()
    data class JsonString(val value: String) : JsonValue()
    data class JsonBool(val value: Boolean) : JsonValue()
    object JsonNull : JsonValue()
}

fun jsonObjectOf(vararg pairs: Pair<String, JsonValue>): JsonValue.JsonObject {
    val obj = JsonValue.JsonObject()
    for ((key, value) in pairs) obj[key] = value
    return obj
}

fun jsonArrayOf(values: List<JsonValue>): JsonValue.JsonArray = JsonValue.JsonArray(values.toMutableList())

/** Renders this value as indented, human-editable JSON text. */
fun JsonValue.render(indent: Int = 0): String = StringBuilder().also { renderTo(it, indent) }.toString()

private fun JsonValue.renderTo(sb: StringBuilder, indent: Int) {
    val pad = "  ".repeat(indent)
    val childPad = "  ".repeat(indent + 1)
    when (this) {
        is JsonValue.JsonObject -> {
            if (entries.isEmpty()) {
                sb.append("{}")
                return
            }
            sb.append("{\n")
            val iterator = entries.entries.iterator()
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                sb.append(childPad).append(escapeJsonString(key)).append(": ")
                value.renderTo(sb, indent + 1)
                if (iterator.hasNext()) sb.append(',')
                sb.append('\n')
            }
            sb.append(pad).append('}')
        }
        is JsonValue.JsonArray -> {
            if (items.isEmpty()) {
                sb.append("[]")
                return
            }
            sb.append("[\n")
            for ((i, item) in items.withIndex()) {
                sb.append(childPad)
                item.renderTo(sb, indent + 1)
                if (i != items.lastIndex) sb.append(',')
                sb.append('\n')
            }
            sb.append(pad).append(']')
        }
        is JsonValue.JsonString -> sb.append(escapeJsonString(value))
        is JsonValue.JsonBool -> sb.append(value.toString())
        JsonValue.JsonNull -> sb.append("null")
    }
}

private fun escapeJsonString(value: String): String {
    val sb = StringBuilder(value.length + 2)
    sb.append('"')
    for (c in value) {
        when (c) {
            '"' -> sb.append("\\\"")
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            '\r' -> sb.append("\\r")
            '\t' -> sb.append("\\t")
            else -> if (c.code < 0x20) sb.append("\\u%04x".format(c.code)) else sb.append(c)
        }
    }
    sb.append('"')
    return sb.toString()
}

/** Thrown when [MiniJson.parse] encounters text that isn't valid JSON. */
class MiniJsonParseException(message: String) : Exception(message)

object MiniJson {

    fun parse(text: String): JsonValue {
        val cursor = Cursor(text)
        cursor.skipWhitespace()
        val value = cursor.parseValue()
        cursor.skipWhitespace()
        return value
    }

    private class Cursor(private val text: String) {
        var pos = 0

        fun skipWhitespace() {
            while (pos < text.length && text[pos].isWhitespace()) pos++
        }

        private fun expect(c: Char) {
            if (pos >= text.length || text[pos] != c) {
                throw MiniJsonParseException("Expected '$c' at offset $pos")
            }
            pos++
        }

        fun parseValue(): JsonValue {
            skipWhitespace()
            if (pos >= text.length) throw MiniJsonParseException("Unexpected end of input")
            return when (text[pos]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> JsonValue.JsonString(parseString())
                't' -> parseLiteral("true", JsonValue.JsonBool(true))
                'f' -> parseLiteral("false", JsonValue.JsonBool(false))
                'n' -> parseLiteral("null", JsonValue.JsonNull)
                else -> throw MiniJsonParseException("Unexpected character '${text[pos]}' at offset $pos")
            }
        }

        private fun parseLiteral(literal: String, value: JsonValue): JsonValue {
            if (pos + literal.length > text.length || text.substring(pos, pos + literal.length) != literal) {
                throw MiniJsonParseException("Expected '$literal' at offset $pos")
            }
            pos += literal.length
            return value
        }

        private fun parseObject(): JsonValue.JsonObject {
            expect('{')
            val obj = JsonValue.JsonObject()
            skipWhitespace()
            if (pos < text.length && text[pos] == '}') {
                pos++
                return obj
            }
            while (true) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                expect(':')
                val value = parseValue()
                obj[key] = value
                skipWhitespace()
                if (pos < text.length && text[pos] == ',') {
                    pos++
                    continue
                }
                break
            }
            skipWhitespace()
            expect('}')
            return obj
        }

        private fun parseArray(): JsonValue.JsonArray {
            expect('[')
            val arr = JsonValue.JsonArray()
            skipWhitespace()
            if (pos < text.length && text[pos] == ']') {
                pos++
                return arr
            }
            while (true) {
                arr.items.add(parseValue())
                skipWhitespace()
                if (pos < text.length && text[pos] == ',') {
                    pos++
                    continue
                }
                break
            }
            skipWhitespace()
            expect(']')
            return arr
        }

        private fun parseString(): String {
            expect('"')
            val sb = StringBuilder()
            while (true) {
                if (pos >= text.length) throw MiniJsonParseException("Unterminated string")
                val c = text[pos++]
                if (c == '"') break
                if (c == '\\') {
                    if (pos >= text.length) throw MiniJsonParseException("Unterminated escape sequence")
                    when (val esc = text[pos++]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (pos + 4 > text.length) throw MiniJsonParseException("Invalid unicode escape")
                            val hex = text.substring(pos, pos + 4)
                            pos += 4
                            sb.append(hex.toInt(16).toChar())
                        }
                        else -> throw MiniJsonParseException("Invalid escape character '$esc' at offset ${pos - 1}")
                    }
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }
    }
}
