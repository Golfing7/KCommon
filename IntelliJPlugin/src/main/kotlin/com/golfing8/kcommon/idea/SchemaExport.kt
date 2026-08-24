package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project

/**
 * The root shape of a schema-export JSON file: every discovered module's field schema, per config
 * bucket, plus a flat registry of named object types (for `#$Type` tag resolution - see
 * [NamedTypeRegistry]). Written by [SchemaExportAction], read back by [SchemaImportAction] into
 * [ExternalSchemaRegistry].
 *
 * `enums` and `types` are a deduplicated header, written first: every distinct enum value list and
 * Nested object shape appears exactly once there (see [SchemaExportInterner]), and everything else
 * - the `types` entries themselves, and every module's fields - references them by id instead of
 * inlining a copy. Without this, something like KCommon's `XMaterial` (~470 constants) embedded
 * directly at every one of the dozens of places an item type can appear would balloon the file by
 * orders of magnitude.
 *
 * ```json
 * {
 *   "kcommonSchemaVersion": "2",
 *   "enums": { "e0": ["A", "B", ...], ... },
 *   "types": { "<type-name>": { "typeName": "<type-name>", "fields": { "<key>": <fieldType>, ... } }, ... },
 *   "modules": { "<module-id>": { "<bucket>": { "<key>": <fieldType>, ... }, ... }, ... }
 * }
 * ```
 * where a `<fieldType>` is `{"kind": "enumRef", "ref": "<enum-id>"}`, `{"kind": "nestedRef", "ref": "<type-name>"}`,
 * `{"kind": "list"/"map", ...}`, or `{"kind": "unknown"}` - see [ConfigFieldTypeJson.toJson]. A file
 * exported before deduplication was added instead inlined enum values/nested fields directly as
 * `"enum"`/`"nested"` - [SchemaImportResolver] still reads those too.
 */
object SchemaExport {

    const val FORMAT_VERSION = 2

    data class Module(val moduleId: String, val buckets: Map<String, Map<String, ConfigFieldType>>)

    fun write(modules: List<Module>, namedTypes: Map<String, ConfigFieldType>, project: Project): String {
        val interner = SchemaExportInterner(project)

        val modulesJson = JsonValue.JsonObject()
        for (module in modules) {
            val bucketsJson = JsonValue.JsonObject()
            for ((bucket, fields) in module.buckets) {
                bucketsJson[bucket] = ConfigFieldTypeJson.fieldsToJson(fields, project, interner)
            }
            modulesJson[module.moduleId] = bucketsJson
        }

        // Also register any named type not necessarily reachable from a module's own fields (e.g. a
        // CASerializable class nothing currently exported happens to reference), purely so `#$Type`
        // can resolve it later - see SchemaExportAction.
        for ((name, type) in namedTypes) {
            if (type is ConfigFieldType.Nested) {
                interner.internNested(name, ConfigFieldTypeJson.fieldsToJson(type.fields, project, interner))
            }
        }

        val root = JsonValue.JsonObject()
        root["kcommonSchemaVersion"] = JsonValue.JsonString(FORMAT_VERSION.toString())
        root["enums"] = interner.enumsJson()
        root["types"] = interner.typesJson()
        root["modules"] = modulesJson

        return root.render()
    }

    data class Parsed(
        val modules: Map<String, Map<String, Map<String, ConfigFieldType>>>,
        val types: Map<String, ConfigFieldType>
    ) {
        companion object {
            val EMPTY = Parsed(emptyMap(), emptyMap())
        }
    }

    /** Never throws - malformed input just yields fewer (or zero) resolvable entries, per this plugin's usual leniency. */
    fun parse(text: String): Parsed {
        val root = runCatching { MiniJson.parse(text) }.getOrNull() as? JsonValue.JsonObject ?: return Parsed.EMPTY

        val rawEnums: Map<String, JsonValue> = (root["enums"] as? JsonValue.JsonObject)?.entries.orEmpty()
        val rawTypes: Map<String, JsonValue> = (root["types"] as? JsonValue.JsonObject)?.entries.orEmpty()
        val resolver = SchemaImportResolver(rawEnums, rawTypes)

        val modules = LinkedHashMap<String, Map<String, Map<String, ConfigFieldType>>>()
        val modulesJson = root["modules"] as? JsonValue.JsonObject
        if (modulesJson != null) {
            for ((moduleId, bucketsValue) in modulesJson.entries) {
                val bucketsJson = bucketsValue as? JsonValue.JsonObject ?: continue
                val buckets = LinkedHashMap<String, Map<String, ConfigFieldType>>()
                for ((bucket, fieldsValue) in bucketsJson.entries) {
                    buckets[bucket] = resolver.resolveFields(fieldsValue)
                }
                modules[moduleId] = buckets
            }
        }

        val types = LinkedHashMap<String, ConfigFieldType>()
        for (alias in rawTypes.keys) {
            types[alias] = resolver.resolveTypeRef(alias)
        }

        return Parsed(modules, types)
    }
}
