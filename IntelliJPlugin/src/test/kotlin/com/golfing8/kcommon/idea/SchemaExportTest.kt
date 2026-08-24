package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SchemaExportTest {

    @Test
    fun `parses a full ref-based export document into modules, buckets and named types`() {
        val text = """
            {
              "kcommonSchemaVersion": "2",
              "enums": {
                "e0": ["DIAMOND_SWORD", "STICK"]
              },
              "types": {
                "ItemStackBuilder": { "typeName": "ItemStackBuilder", "fields": {
                  "type": { "kind": "enumRef", "ref": "e0" }
                } },
                "ArenaReward": { "typeName": "ArenaReward", "fields": {
                  "amount": { "kind": "unknown" },
                  "item": { "kind": "nestedRef", "ref": "ItemStackBuilder" }
                } }
              },
              "modules": {
                "arena": {
                  "config": {
                    "max-players": { "kind": "unknown" },
                    "reward": { "kind": "nestedRef", "ref": "ArenaReward" }
                  },
                  "limits": {
                    "max-arenas": { "kind": "unknown" }
                  }
                }
              }
            }
        """.trimIndent()

        val parsed = SchemaExport.parse(text)

        assertEquals(setOf("arena"), parsed.modules.keys)
        val arena = parsed.modules.getValue("arena")
        assertEquals(setOf("config", "limits"), arena.keys)

        val configFields = arena.getValue("config")
        assertTrue(configFields.containsKey("max-players"))
        val reward = configFields.getValue("reward") as ConfigFieldType.Nested
        assertEquals("ArenaReward", reward.typeName)
        assertEquals(ConfigFieldType.Unknown, reward.fields["amount"])

        // The reward's item field dereferences through to the shared ItemStackBuilder entry, enum and all.
        val item = reward.fields["item"] as ConfigFieldType.Nested
        assertEquals("ItemStackBuilder", item.typeName)
        val enumType = item.fields["type"] as ConfigFieldType.EnumLike
        assertEquals(listOf("DIAMOND_SWORD", "STICK"), (enumType.source as EnumSource.FixedValues).values)

        val limitsFields = arena.getValue("limits")
        assertTrue(limitsFields.containsKey("max-arenas"))

        assertEquals(setOf("ItemStackBuilder", "ArenaReward"), parsed.types.keys)
    }

    @Test
    fun `a pre-interning export (inline enum and nested kinds, no enums or types header) still parses correctly`() {
        val text = """
            {
              "kcommonSchemaVersion": "1",
              "modules": {
                "arena": {
                  "config": {
                    "reward": {
                      "kind": "nested",
                      "typeName": "ArenaReward",
                      "fields": {
                        "type": { "kind": "enum", "values": ["A", "B"] }
                      }
                    }
                  }
                }
              },
              "types": {}
            }
        """.trimIndent()

        val parsed = SchemaExport.parse(text)
        val reward = parsed.modules.getValue("arena").getValue("config").getValue("reward") as ConfigFieldType.Nested
        assertEquals("ArenaReward", reward.typeName)
        val enumType = reward.fields["type"] as ConfigFieldType.EnumLike
        assertEquals(listOf("A", "B"), (enumType.source as EnumSource.FixedValues).values)
    }

    @Test
    fun `malformed json yields an empty parse result, never an exception`() {
        val parsed = SchemaExport.parse("not json at all { [ }")
        assertEquals(SchemaExport.Parsed.EMPTY, parsed)
    }

    @Test
    fun `missing enums, types or modules sections yield empty maps for those, not an error`() {
        val parsed = SchemaExport.parse("""{"kcommonSchemaVersion": "2"}""")
        assertTrue(parsed.modules.isEmpty())
        assertTrue(parsed.types.isEmpty())
    }

    @Test
    fun `an enum referenced from many places is written to the enums table only once`() {
        val bigEnum = ConfigFieldType.EnumLike(EnumSource.FixedValues(listOf("A", "B", "C")))
        val itemType = ConfigFieldType.Nested("ItemStackBuilder", mapOf("type" to bigEnum))
        val modules = listOf(
            SchemaExport.Module(
                "arena", mapOf(
                    "config" to mapOf(
                        "item-one" to itemType,
                        "item-two" to itemType,
                        "items" to ConfigFieldType.ListOf(itemType)
                    )
                )
            )
        )

        val json = SchemaExport.write(modules, emptyMap(), FakeProject)
        val root = MiniJson.parse(json) as JsonValue.JsonObject

        val enums = root["enums"] as JsonValue.JsonObject
        assertEquals(1, enums.entries.size)

        val types = root["types"] as JsonValue.JsonObject
        assertEquals(1, types.entries.size)
    }
}
