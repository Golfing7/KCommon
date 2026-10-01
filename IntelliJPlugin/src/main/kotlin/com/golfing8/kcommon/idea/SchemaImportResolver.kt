package com.golfing8.kcommon.idea

/**
 * Dereferences the `enumRef`/`nestedRef` tokens [SchemaExportInterner] produces back into a
 * [ConfigFieldType] tree - the read-side counterpart of [ConfigFieldTypeJson.toJson]. Also still
 * understands the older, pre-interning inline `enum`/`nested` shapes (a full value list / field map
 * written directly in place), so a schema file exported before deduplication was added keeps working.
 *
 * Resolved types are memoized by alias, both because the same alias is typically referenced many
 * times (that's the whole point) and to guard against a hand-edited or otherwise malformed file
 * whose `types` table contains a reference cycle (`"A"` referencing `"B"` referencing `"A"`) -
 * [resolvingAliases] detects reentrancy and degrades the closing reference to [ConfigFieldType.Unknown]
 * rather than recursing forever, the same leniency [ConfigPsiUtil.collectConfFields] applies to a
 * genuinely cyclic Java type.
 */
class SchemaImportResolver(private val rawEnums: Map<String, JsonValue>, private val rawTypes: Map<String, JsonValue>) {

    private val resolvedEnums = HashMap<String, List<String>>()
    private val resolvedTypes = HashMap<String, ConfigFieldType>()
    private val resolvingAliases = HashSet<String>()

    /** Null when [ref] isn't in the enums table at all - distinct from a present-but-empty value list, so [resolveField] can degrade a dangling reference to [ConfigFieldType.Unknown] rather than a spuriously valid, always-empty enum. */
    fun resolveEnumRef(ref: String): List<String>? {
        resolvedEnums[ref]?.let { return it }
        val raw = rawEnums[ref] as? JsonValue.JsonArray ?: return null
        val values = raw.items.mapNotNull { (it as? JsonValue.JsonString)?.value }
        resolvedEnums[ref] = values
        return values
    }

    /**
     * A `types` table entry is either the current `{"typeName": ..., "fields": {...}}` record, or -
     * for a file exported before deduplication - a full `kind`-tagged fieldType blob (in practice
     * always `"kind": "nested"`, since only Nested types were ever exported there, but [resolveField]
     * handles it generically regardless).
     */
    fun resolveTypeRef(ref: String): ConfigFieldType {
        resolvedTypes[ref]?.let { return it }
        if (!resolvingAliases.add(ref)) return ConfigFieldType.Unknown
        try {
            val raw = rawTypes[ref] as? JsonValue.JsonObject ?: return ConfigFieldType.Unknown
            val result = if (raw.entries.containsKey("kind")) {
                resolveField(raw)
            } else {
                val typeName = (raw["typeName"] as? JsonValue.JsonString)?.value ?: ref
                ConfigFieldType.Nested(typeName, resolveFields(raw["fields"]))
            }
            resolvedTypes[ref] = result
            return result
        } finally {
            resolvingAliases.remove(ref)
        }
    }

    /** Never throws - malformed/unrecognized shapes degrade to [ConfigFieldType.Unknown], per this plugin's usual leniency. */
    fun resolveField(value: JsonValue?): ConfigFieldType {
        val obj = value as? JsonValue.JsonObject ?: return ConfigFieldType.Unknown
        return when ((obj["kind"] as? JsonValue.JsonString)?.value) {
            "enumRef" -> {
                val ref = (obj["ref"] as? JsonValue.JsonString)?.value ?: return ConfigFieldType.Unknown
                val values = resolveEnumRef(ref) ?: return ConfigFieldType.Unknown
                ConfigFieldType.EnumLike(EnumSource.FixedValues(values))
            }
            "enum" -> { // legacy: values inlined directly, pre-dating SchemaExportInterner
                val values = (obj["values"] as? JsonValue.JsonArray)?.items
                    ?.mapNotNull { (it as? JsonValue.JsonString)?.value }
                    .orEmpty()
                ConfigFieldType.EnumLike(EnumSource.FixedValues(values))
            }
            "nestedRef" -> {
                val ref = (obj["ref"] as? JsonValue.JsonString)?.value ?: return ConfigFieldType.Unknown
                resolveTypeRef(ref)
            }
            "nested" -> { // legacy: fields inlined directly, pre-dating SchemaExportInterner
                val typeName = (obj["typeName"] as? JsonValue.JsonString)?.value ?: "object"
                ConfigFieldType.Nested(typeName, resolveFields(obj["fields"]))
            }
            "list" -> ConfigFieldType.ListOf(resolveField(obj["inner"]))
            "map" -> ConfigFieldType.MapOf(
                resolveField(obj["inner"]),
                (resolveField(obj["keyType"])) as? ConfigFieldType.EnumLike ?: ConfigFieldType.Unknown
            )
            else -> ConfigFieldType.Unknown
        }
    }

    fun resolveFields(value: JsonValue?): Map<String, ConfigFieldType> {
        val obj = value as? JsonValue.JsonObject ?: return emptyMap()
        val result = LinkedHashMap<String, ConfigFieldType>()
        for ((key, v) in obj.entries) result[key] = resolveField(v)
        return result
    }
}
