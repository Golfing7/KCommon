package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

/**
 * Exercises the interning logic directly (bypassing [SchemaExportInterner.resolveEnumValues], which
 * needs a real [com.intellij.openapi.project.Project] to call [EnumSource.resolve]) - these methods
 * don't touch PSI at all, they're pure content-addressed deduplication.
 */
class SchemaExportInternerTest {

    @Test
    fun `interning the same enum values twice returns the same ref`() {
        val interner = SchemaExportInterner(FakeProject)
        val ref1 = interner.internEnum(listOf("A", "B", "C"))
        val ref2 = interner.internEnum(listOf("A", "B", "C"))

        assertEquals(ref1, ref2)
        assertEquals(1, interner.enumsJson().entries.size)
    }

    @Test
    fun `different enum value lists get different refs and both are written out`() {
        val interner = SchemaExportInterner(FakeProject)
        val ref1 = interner.internEnum(listOf("A", "B"))
        val ref2 = interner.internEnum(listOf("X", "Y", "Z"))

        assertNotEquals(ref1, ref2)
        val enums = interner.enumsJson()
        assertEquals(2, enums.entries.size)
        assertEquals(listOf("A", "B"), (enums[ref1] as JsonValue.JsonArray).items.map { (it as JsonValue.JsonString).value })
        assertEquals(listOf("X", "Y", "Z"), (enums[ref2] as JsonValue.JsonArray).items.map { (it as JsonValue.JsonString).value })
    }

    @Test
    fun `interning an identical nested shape twice under the same name reuses the alias`() {
        val interner = SchemaExportInterner(FakeProject)
        val fields = jsonObjectOf("amount" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")))

        val alias1 = interner.internNested("Drop", fields)
        val alias2 = interner.internNested("Drop", fields)

        assertEquals("Drop", alias1)
        assertEquals(alias1, alias2)
        assertEquals(1, interner.typesJson().entries.size)
    }

    @Test
    fun `a different shape under the same type name gets a disambiguating suffix rather than colliding`() {
        val interner = SchemaExportInterner(FakeProject)
        val normalFields = jsonObjectOf("size" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")))
        val pagedFields = jsonObjectOf(
            "size" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")),
            "max-page" to jsonObjectOf("kind" to JsonValue.JsonString("unknown"))
        )

        val normalAlias = interner.internNested("MenuBuilder", normalFields)
        val pagedAlias = interner.internNested("MenuBuilder", pagedFields)

        assertEquals("MenuBuilder", normalAlias)
        assertNotEquals(normalAlias, pagedAlias)
        assertEquals("MenuBuilder", (interner.typesJson()[pagedAlias] as JsonValue.JsonObject).let {
            ((it["typeName"]) as JsonValue.JsonString).value
        })
        assertEquals(2, interner.typesJson().entries.size)
    }

    @Test
    fun `re-registering the exact same shape under a name that already has a disambiguated alias reuses it`() {
        val interner = SchemaExportInterner(FakeProject)
        val a = jsonObjectOf("a" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")))
        val b = jsonObjectOf("b" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")))

        val aliasA1 = interner.internNested("Shape", a)
        val aliasB = interner.internNested("Shape", b)
        val aliasA2 = interner.internNested("Shape", a)

        assertEquals(aliasA1, aliasA2)
        assertNotEquals(aliasA1, aliasB)
        assertEquals(2, interner.typesJson().entries.size)
    }
}
