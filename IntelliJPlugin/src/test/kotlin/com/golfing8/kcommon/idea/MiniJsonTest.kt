package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MiniJsonTest {

    @Test
    fun `parses a nested object with arrays, strings and booleans`() {
        val text = """
            {
              "a": "hello",
              "b": true,
              "c": false,
              "d": null,
              "e": ["x", "y", "z"],
              "f": { "g": "h" },
              "empty-obj": {},
              "empty-arr": []
            }
        """.trimIndent()

        val value = MiniJson.parse(text) as JsonValue.JsonObject
        assertEquals("hello", (value["a"] as JsonValue.JsonString).value)
        assertEquals(true, (value["b"] as JsonValue.JsonBool).value)
        assertEquals(false, (value["c"] as JsonValue.JsonBool).value)
        assertTrue(value["d"] is JsonValue.JsonNull)
        assertEquals(listOf("x", "y", "z"), (value["e"] as JsonValue.JsonArray).items.map { (it as JsonValue.JsonString).value })
        assertEquals("h", ((value["f"] as JsonValue.JsonObject)["g"] as JsonValue.JsonString).value)
        assertTrue((value["empty-obj"] as JsonValue.JsonObject).entries.isEmpty())
        assertTrue((value["empty-arr"] as JsonValue.JsonArray).items.isEmpty())
    }

    @Test
    fun `unescapes standard escape sequences and unicode escapes`() {
        val text = """{"s": "line1\nline2\ttab\\backslash\"quoteA"}"""
        val value = MiniJson.parse(text) as JsonValue.JsonObject
        assertEquals("line1\nline2\ttab\\backslash\"quoteA", (value["s"] as JsonValue.JsonString).value)
    }

    @Test
    fun `render then reparse round-trips structurally`() {
        val original = jsonObjectOf(
            "kind" to JsonValue.JsonString("nested"),
            "typeName" to JsonValue.JsonString("Foo\"Bar"),
            "fields" to jsonObjectOf(
                "list" to jsonArrayOf(listOf(JsonValue.JsonString("a"), JsonValue.JsonString("b"))),
                "flag" to JsonValue.JsonBool(true)
            )
        )

        val rendered = original.render()
        val reparsed = MiniJson.parse(rendered) as JsonValue.JsonObject

        assertEquals("nested", (reparsed["kind"] as JsonValue.JsonString).value)
        assertEquals("Foo\"Bar", (reparsed["typeName"] as JsonValue.JsonString).value)
        val fields = reparsed["fields"] as JsonValue.JsonObject
        assertEquals(listOf("a", "b"), (fields["list"] as JsonValue.JsonArray).items.map { (it as JsonValue.JsonString).value })
        assertEquals(true, (fields["flag"] as JsonValue.JsonBool).value)
    }

    @Test
    fun `malformed input throws MiniJsonParseException rather than a generic error`() {
        assertThrows(MiniJsonParseException::class.java) { MiniJson.parse("{not valid json") }
        assertThrows(MiniJsonParseException::class.java) { MiniJson.parse("") }
        assertThrows(MiniJsonParseException::class.java) { MiniJson.parse("{\"a\": }") }
    }
}
