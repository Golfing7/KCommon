package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** Exercises [ExternalSchemaRegistry] directly (bypassing the IntelliJ service container - it needs no Project) against real files on disk, since its whole job is file IO + JSON merging. */
class ExternalSchemaRegistryTest {

    private fun schemaFile(dir: Path, name: String, text: String): String {
        val file = dir.resolve(name)
        Files.writeString(file, text)
        return file.toString()
    }

    @Test
    fun `resolves fields and named types from a single imported file`(@TempDir dir: Path) {
        val path = schemaFile(
            dir, "one.json", """
            {
              "modules": { "arena": { "config": { "max-players": { "kind": "unknown" } } } },
              "types": { "ArenaReward": { "kind": "nested", "typeName": "ArenaReward", "fields": {} } }
            }
            """.trimIndent()
        )

        val registry = ExternalSchemaRegistry()
        val failures = registry.addFiles(listOf(path))

        assertTrue(failures.isEmpty())
        assertEquals(setOf("max-players"), registry.fieldsFor("arena", "config")?.keys)
        assertNull(registry.fieldsFor("arena", "limits"))
        assertNull(registry.fieldsFor("unknown-module", "config"))
        assertTrue(registry.namedType("ArenaReward") is ConfigFieldType.Nested)
        assertEquals(listOf("arena"), registry.allModuleIds())
        assertEquals(listOf("config"), registry.allBuckets("arena"))
        assertEquals(listOf("ArenaReward"), registry.allTypeNames())
    }

    @Test
    fun `module and bucket lookups are case-insensitive`(@TempDir dir: Path) {
        val path = schemaFile(
            dir, "one.json",
            """{"modules": { "Arena": { "Config": { "k": { "kind": "unknown" } } } } }"""
        )
        val registry = ExternalSchemaRegistry()
        registry.addFiles(listOf(path))

        assertEquals(setOf("k"), registry.fieldsFor("arena", "config")?.keys)
        assertEquals(setOf("k"), registry.fieldsFor("ARENA", "CONFIG")?.keys)
    }

    @Test
    fun `a later-imported file overrides an earlier one on the same module+bucket`(@TempDir dir: Path) {
        val first = schemaFile(dir, "first.json", """{"modules": {"arena": {"config": {"old-key": {"kind": "unknown"}}}}}""")
        val second = schemaFile(dir, "second.json", """{"modules": {"arena": {"config": {"new-key": {"kind": "unknown"}}}}}""")

        val registry = ExternalSchemaRegistry()
        registry.addFiles(listOf(first))
        registry.addFiles(listOf(second))

        val fields = registry.fieldsFor("arena", "config")
        assertEquals(setOf("new-key"), fields?.keys)
    }

    @Test
    fun `unreadable files are reported as failures and not imported`(@TempDir dir: Path) {
        val registry = ExternalSchemaRegistry()
        val missing = dir.resolve("does-not-exist.json").toString()

        val failures = registry.addFiles(listOf(missing))

        assertEquals(listOf(missing), failures)
        assertTrue(registry.importedFilePaths().isEmpty())
    }

    @Test
    fun `removeFile drops both the path and its contributed data`(@TempDir dir: Path) {
        val path = schemaFile(dir, "one.json", """{"modules": {"arena": {"config": {"k": {"kind": "unknown"}}}}}""")
        val registry = ExternalSchemaRegistry()
        registry.addFiles(listOf(path))
        assertTrue(registry.importedFilePaths().contains(path))

        registry.removeFile(path)

        assertTrue(registry.importedFilePaths().isEmpty())
        assertNull(registry.fieldsFor("arena", "config"))
    }

    @Test
    fun `reload re-reads from disk and drops files that no longer exist`(@TempDir dir: Path) {
        val path = schemaFile(dir, "one.json", """{"modules": {"arena": {"config": {"k": {"kind": "unknown"}}}}}""")
        val registry = ExternalSchemaRegistry()
        registry.addFiles(listOf(path))

        Files.delete(dir.resolve("one.json"))
        registry.reload()

        assertNull(registry.fieldsFor("arena", "config"))
    }
}
