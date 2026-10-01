package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SchemaImportResolverTest {

    @Test
    fun `resolves an enumRef against the enums table`() {
        val enums = mapOf("e0" to jsonArrayOf(listOf(JsonValue.JsonString("A"), JsonValue.JsonString("B"))))
        val resolver = SchemaImportResolver(enums, emptyMap())

        val field = jsonObjectOf("kind" to JsonValue.JsonString("enumRef"), "ref" to JsonValue.JsonString("e0"))
        val type = resolver.resolveField(field) as ConfigFieldType.EnumLike
        assertEquals(listOf("A", "B"), (type.source as EnumSource.FixedValues).values)
    }

    @Test
    fun `resolves a nestedRef against the types table using the new typeName+fields record shape`() {
        val types = mapOf(
            "ItemStackBuilder" to jsonObjectOf(
                "typeName" to JsonValue.JsonString("ItemStackBuilder"),
                "fields" to jsonObjectOf("amount" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")))
            )
        )
        val resolver = SchemaImportResolver(emptyMap(), types)

        val field = jsonObjectOf("kind" to JsonValue.JsonString("nestedRef"), "ref" to JsonValue.JsonString("ItemStackBuilder"))
        val type = resolver.resolveField(field) as ConfigFieldType.Nested
        assertEquals("ItemStackBuilder", type.typeName)
        assertEquals(ConfigFieldType.Unknown, type.fields["amount"])
    }

    @Test
    fun `resolving the same nestedRef twice returns the same memoized instance`() {
        val types = mapOf(
            "Drop" to jsonObjectOf(
                "typeName" to JsonValue.JsonString("Drop"),
                "fields" to jsonObjectOf()
            )
        )
        val resolver = SchemaImportResolver(emptyMap(), types)

        val first = resolver.resolveTypeRef("Drop")
        val second = resolver.resolveTypeRef("Drop")
        assertSame(first, second)
    }

    @Test
    fun `a legacy kind-tagged types table entry (pre-dating interning) still resolves`() {
        val types = mapOf(
            "Legacy" to jsonObjectOf(
                "kind" to JsonValue.JsonString("nested"),
                "typeName" to JsonValue.JsonString("Legacy"),
                "fields" to jsonObjectOf("x" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")))
            )
        )
        val resolver = SchemaImportResolver(emptyMap(), types)

        val type = resolver.resolveTypeRef("Legacy") as ConfigFieldType.Nested
        assertEquals("Legacy", type.typeName)
        assertTrue(type.fields.containsKey("x"))
    }

    @Test
    fun `legacy inline enum and nested kinds (pre-dating interning) still resolve directly`() {
        val resolver = SchemaImportResolver(emptyMap(), emptyMap())

        val enumField = jsonObjectOf(
            "kind" to JsonValue.JsonString("enum"),
            "values" to jsonArrayOf(listOf(JsonValue.JsonString("X")))
        )
        assertEquals(listOf("X"), ((resolver.resolveField(enumField) as ConfigFieldType.EnumLike).source as EnumSource.FixedValues).values)

        val nestedField = jsonObjectOf(
            "kind" to JsonValue.JsonString("nested"),
            "typeName" to JsonValue.JsonString("Foo"),
            "fields" to jsonObjectOf("y" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")))
        )
        val nested = resolver.resolveField(nestedField) as ConfigFieldType.Nested
        assertEquals("Foo", nested.typeName)
        assertTrue(nested.fields.containsKey("y"))
    }

    @Test
    fun `a reference cycle in the types table degrades to Unknown instead of overflowing the stack`() {
        val types = mapOf(
            "A" to jsonObjectOf(
                "typeName" to JsonValue.JsonString("A"),
                "fields" to jsonObjectOf(
                    "b" to jsonObjectOf("kind" to JsonValue.JsonString("nestedRef"), "ref" to JsonValue.JsonString("B"))
                )
            ),
            "B" to jsonObjectOf(
                "typeName" to JsonValue.JsonString("B"),
                "fields" to jsonObjectOf(
                    "a" to jsonObjectOf("kind" to JsonValue.JsonString("nestedRef"), "ref" to JsonValue.JsonString("A"))
                )
            )
        )
        val resolver = SchemaImportResolver(emptyMap(), types)

        val a = resolver.resolveTypeRef("A") as ConfigFieldType.Nested
        assertEquals("A", a.typeName)
        val b = a.fields["b"] as ConfigFieldType.Nested
        assertEquals("B", b.typeName)
        // The edge that closes the cycle degrades to Unknown rather than recursing forever.
        assertEquals(ConfigFieldType.Unknown, b.fields["a"])
    }

    @Test
    fun `an unresolvable ref or malformed shape degrades to Unknown rather than throwing`() {
        val resolver = SchemaImportResolver(emptyMap(), emptyMap())
        assertEquals(
            ConfigFieldType.Unknown,
            resolver.resolveField(jsonObjectOf("kind" to JsonValue.JsonString("nestedRef"), "ref" to JsonValue.JsonString("missing")))
        )
        assertEquals(
            ConfigFieldType.Unknown,
            resolver.resolveField(jsonObjectOf("kind" to JsonValue.JsonString("enumRef"), "ref" to JsonValue.JsonString("missing")))
        )
        assertEquals(ConfigFieldType.Unknown, resolver.resolveField(null))
        assertEquals(ConfigFieldType.Unknown, resolver.resolveField(JsonValue.JsonBool(true)))
    }

    @Test
    fun `list and map wrap their resolved inner type`() {
        val enums = mapOf("e0" to jsonArrayOf(listOf(JsonValue.JsonString("A"))))
        val resolver = SchemaImportResolver(enums, emptyMap())

        val listField = jsonObjectOf(
            "kind" to JsonValue.JsonString("list"),
            "inner" to jsonObjectOf("kind" to JsonValue.JsonString("enumRef"), "ref" to JsonValue.JsonString("e0"))
        )
        val list = resolver.resolveField(listField) as ConfigFieldType.ListOf
        assertTrue(list.inner is ConfigFieldType.EnumLike)

        val mapField = jsonObjectOf(
            "kind" to JsonValue.JsonString("map"),
            "inner" to jsonObjectOf("kind" to JsonValue.JsonString("unknown")),
            "keyType" to jsonObjectOf("kind" to JsonValue.JsonString("enumRef"), "ref" to JsonValue.JsonString("e0"))
        )
        val map = resolver.resolveField(mapField) as ConfigFieldType.MapOf
        assertEquals(ConfigFieldType.Unknown, map.inner)
        assertTrue(map.keyType is ConfigFieldType.EnumLike)
    }
}
