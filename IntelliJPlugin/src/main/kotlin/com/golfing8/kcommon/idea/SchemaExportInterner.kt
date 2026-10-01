package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project

/**
 * Deduplicates the two things that made naive schema exports balloon in size: enum value lists
 * (e.g. XMaterial's ~470 constants) and Nested object shapes (e.g. ItemStackBuilder), both of which
 * can otherwise appear dozens of times across a project's modules - every occurrence inlining a full
 * independent copy, and multiplicatively so for a Nested type that itself contains a large enum.
 *
 * Used only during [SchemaExport.write]: every enum/Nested value encountered is registered once
 * here and thereafter referenced by a short id (`"kind": "enumRef"` / `"kind": "nestedRef"`) instead
 * of being inlined again - see [ConfigFieldTypeJson.toJson]. The actual tables are written once, at
 * the top of the export, via [enumsJson]/[typesJson].
 */
class SchemaExportInterner(private val project: Project) {

    private val enumIds = LinkedHashMap<List<String>, String>()

    /** Memoizes [EnumSource.resolve] itself too - the same [EnumSource] (e.g. XMaterial's) can otherwise be walked via PSI dozens of times over the course of one export. */
    private val enumResolutionCache = HashMap<EnumSource, List<String>>()

    /** alias (usually just the type's name, disambiguated with a `#2`/`#3`/... suffix only on a genuine same-name-different-shape collision) -> that shape's already-reference-based fields JSON. */
    private val nestedTypes = LinkedHashMap<String, JsonValue.JsonObject>()

    /** typeName -> every (alias, canonical signature text) registered so far under that name, for collision detection. */
    private val aliasesByTypeName = LinkedHashMap<String, MutableList<Pair<String, String>>>()

    fun resolveEnumValues(source: EnumSource): List<String> =
        enumResolutionCache.getOrPut(source) { EnumSource.resolve(source, project) }

    /** Registers [values] (if not already known) and returns its reference id. */
    fun internEnum(values: List<String>): String = enumIds.getOrPut(values) { "e${enumIds.size}" }

    /**
     * Registers a Nested type's already-reference-based [fieldsJson] under [typeName] (if an
     * identical shape isn't already registered) and returns the alias to reference it by - normally
     * just [typeName] itself, unless a *different* shape already claimed that name (e.g. KCommon's
     * synthetic "MenuBuilder" shape varies between a normal and a paged menu container), in which
     * case a disambiguating suffix is appended.
     */
    fun internNested(typeName: String, fieldsJson: JsonValue.JsonObject): String {
        val signature = fieldsJson.render()
        val existing = aliasesByTypeName.getOrPut(typeName) { mutableListOf() }
        existing.firstOrNull { it.second == signature }?.let { return it.first }

        val alias = if (existing.isEmpty()) typeName else "$typeName#${existing.size + 1}"
        existing.add(alias to signature)
        nestedTypes[alias] = jsonObjectOf("typeName" to JsonValue.JsonString(typeName), "fields" to fieldsJson)
        return alias
    }

    fun enumsJson(): JsonValue.JsonObject {
        val obj = JsonValue.JsonObject()
        for ((values, id) in enumIds) obj[id] = jsonArrayOf(values.map { JsonValue.JsonString(it) })
        return obj
    }

    fun typesJson(): JsonValue.JsonObject {
        val obj = JsonValue.JsonObject()
        for ((alias, typeJson) in nestedTypes) obj[alias] = typeJson
        return obj
    }
}
