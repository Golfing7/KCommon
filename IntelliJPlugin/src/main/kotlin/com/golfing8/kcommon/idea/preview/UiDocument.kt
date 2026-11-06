package com.golfing8.kcommon.idea.preview

/**
 * The top-level `@Name = value;` definitions of a Hytale `.ui` document, such as
 * `Common/UI/Custom/Common.ui`, so the menu preview can look up the styles a menu references.
 *
 * Values resolve to plain data: tuples (`PatchStyle(...)`, `(...)`) become maps with their
 * `...@Spread` entries merged in, numbers become doubles (`9 + 8` is added up), and colors, strings
 * and names become strings. Element templates (`@TextButton = TextButton { ... }`) are skipped.
 * Parsing is lenient: a definition that doesn't parse is dropped and the rest are kept.
 */
class UiDocument private constructor(
    private val definitions: Map<String, Any?>,
    /** `$Sounds = "Sounds.ui";` aliases, by name. */
    private val aliases: Map<String, String>,
    private val load: (String) -> UiDocument?,
) {
    private val resolving = HashSet<String>()
    private val resolved = HashMap<String, Any?>()

    /** The resolved value of `@name`, or null when it isn't defined or can't be resolved. */
    operator fun get(name: String): Any? {
        if (name in resolved) return resolved[name]
        if (!resolving.add(name)) return null
        try {
            return resolve(definitions[name]).also { resolved[name] = it }
        } finally {
            resolving.remove(name)
        }
    }

    /** Every name defined here, including element templates (whose value is null). */
    val names: Set<String> get() = definitions.keys

    fun has(name: String): Boolean = name in definitions

    /** The `.ui` type a definition was written as (`@Foo = TextButtonStyle(...)` is a TextButtonStyle), following references; null when unknown. */
    fun typeOf(name: String, depth: Int = 0): String? = when (val value = definitions[name]) {
        is Tuple -> value.type
        is Ref -> if (depth > 8) null else if (value.alias == null) typeOf(value.name, depth + 1) else aliases[value.alias]?.let(load)?.typeOf(value.name, depth + 1)
        else -> null
    }

    private fun resolve(value: Any?): Any? = when (value) {
        is Ref -> if (value.alias == null) get(value.name) else aliases[value.alias]?.let(load)?.get(value.name)
        is Tuple -> {
            val map = LinkedHashMap<String, Any?>()
            for ((key, entry) in value.entries) {
                val v = resolve(entry)
                if (key == null) (v as? Map<*, *>)?.forEach { (k, inner) -> map[k.toString()] = inner } else map[key] = v
            }
            map
        }
        else -> value
    }

    private data class Ref(val alias: String?, val name: String)
    private class Tuple(val entries: List<Pair<String?, Any?>>, val type: String? = null)

    companion object {
        fun parse(text: String, load: (String) -> UiDocument? = { null }): UiDocument {
            val parser = Parser(text)
            parser.document()
            return UiDocument(parser.definitions, parser.aliases, load)
        }
    }

    private class Parser(private val text: String) {
        val definitions = LinkedHashMap<String, Any?>()
        val aliases = LinkedHashMap<String, String>()
        private var i = 0

        fun document() {
            while (true) {
                skipSpace()
                if (i >= text.length) return
                val start = i
                val sigil = text[i]
                if (sigil == '@' || sigil == '$') {
                    i++
                    val name = ident()
                    skipSpace()
                    if (name.isNotEmpty() && peek() == '=') {
                        i++
                        val value = runCatching { value() }.getOrNull()
                        if (sigil == '@') definitions[name] = value else (value as? String)?.let { aliases[name] = it }
                    }
                }
                skipStatement()
                if (i == start) i++
            }
        }

        private fun value(): Any? {
            skipSpace()
            val c = peek() ?: error("end of document")
            return when {
                c == '"' -> string()
                c == '#' -> color()
                c == '(' -> tuple(null)
                c == '@' -> { i++; Ref(null, ident()) }
                c == '$' -> {
                    i++
                    val alias = ident()
                    expect('.'); expect('@')
                    Ref(alias, ident())
                }
                c.isDigit() || c == '-' -> arithmetic()
                c.isLetter() || c == '_' -> {
                    val name = ident()
                    skipSpace()
                    when (peek()) {
                        '(' -> tuple(name)
                        '{' -> { skipBlock(); null }
                        else -> name
                    }
                }
                else -> error("unexpected '$c'")
            }
        }

        private fun tuple(type: String?): Tuple {
            expect('(')
            val entries = ArrayList<Pair<String?, Any?>>()
            while (true) {
                skipSpace()
                if (peek() == ')') { i++; return Tuple(entries, type) }
                if (text.startsWith("...", i)) {
                    i += 3
                    entries += null to value()
                } else {
                    val key = ident()
                    if (key.isEmpty()) error("expected a key")
                    skipSpace(); expect(':')
                    entries += key to value()
                }
                skipSpace()
                if (peek() == ',') i++
            }
        }

        private fun arithmetic(): Double {
            var total = number()
            while (true) {
                val save = i
                skipSpace()
                val op = peek()
                if (op != '+' && op != '-') { i = save; return total }
                i++
                skipSpace()
                val next = number()
                total = if (op == '+') total + next else total - next
            }
        }

        private fun number(): Double {
            val start = i
            if (peek() == '-') i++
            while (i < text.length && (text[i].isDigit() || text[i] == '.')) i++
            return text.substring(start, i).toDoubleOrNull() ?: error("bad number")
        }

        /** `#rrggbb`, `#rrggbbaa`, or `#rrggbb(0.45)`. */
        private fun color(): String {
            val start = i++
            while (i < text.length && text[i].isLetterOrDigit()) i++
            if (peek() == '(') {
                val close = text.indexOf(')', i)
                if (close > 0) i = close + 1
            }
            return text.substring(start, i)
        }

        private fun string(): String {
            expect('"')
            val out = StringBuilder()
            while (i < text.length && text[i] != '"') {
                if (text[i] == '\\' && i + 1 < text.length) i++
                out.append(text[i++])
            }
            expect('"')
            return out.toString()
        }

        private fun ident(): String {
            val start = i
            while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) i++
            return text.substring(start, i)
        }

        private fun expect(c: Char) {
            if (peek() != c) error("expected '$c'")
            i++
        }

        private fun peek(): Char? = text.getOrNull(i)

        /** Skips to just past the `;` ending this statement, or past a `{...}` block that ends it. */
        private fun skipStatement() {
            var depth = 0
            while (i < text.length) {
                when (text[i]) {
                    '"' -> { string(); continue }
                    '/' -> if (text.startsWith("//", i) || text.startsWith("/*", i)) { skipSpace(); continue }
                    '(', '{' -> depth++
                    ')' -> depth--
                    '}' -> {
                        depth--
                        if (depth <= 0) {
                            i++
                            skipSpace()
                            if (peek() == ';') i++
                            return
                        }
                    }
                    ';' -> if (depth <= 0) { i++; return }
                }
                i++
            }
        }

        private fun skipBlock() {
            var depth = 0
            while (i < text.length) {
                when (text[i]) {
                    '"' -> { string(); continue }
                    '{' -> depth++
                    '}' -> if (--depth == 0) { i++; return }
                }
                i++
            }
        }

        private fun skipSpace() {
            while (i < text.length) {
                when {
                    text[i].isWhitespace() -> i++
                    text.startsWith("//", i) -> i = text.indexOf('\n', i).let { if (it < 0) text.length else it }
                    text.startsWith("/*", i) -> i = text.indexOf("*/", i + 2).let { if (it < 0) text.length else it + 2 }
                    else -> return
                }
            }
        }
    }
}
