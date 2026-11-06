package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project

/**
 * Serializes [ConfigFieldType] trees to the plugin's schema-export JSON shape (see [SchemaExport]),
 * deduplicating enum value lists and Nested object shapes via [interner] rather than inlining a full
 * copy at every occurrence - see [SchemaExportInterner]'s doc for why that matters. The counterpart
 * read side (dereferencing those `enumRef`/`nestedRef` tokens back into a tree) is [SchemaImportResolver].
 */
object ConfigFieldTypeJson {

    fun toJson(type: ConfigFieldType, project: Project, interner: SchemaExportInterner): JsonValue = when (type) {
        is ConfigFieldType.EnumLike -> {
            val ref = interner.internEnum(interner.resolveEnumValues(type.source))
            jsonObjectOf("kind" to JsonValue.JsonString("enumRef"), "ref" to JsonValue.JsonString(ref))
        }
        is ConfigFieldType.Nested -> interner.memoize(type) {
            val ref = internNested(type.typeName, type, project, interner)
            jsonObjectOf("kind" to JsonValue.JsonString("nestedRef"), "ref" to JsonValue.JsonString(ref))
        }
        is ConfigFieldType.ListOf -> jsonObjectOf(
            "kind" to JsonValue.JsonString("list"),
            "inner" to toJson(type.inner, project, interner)
        )
        is ConfigFieldType.MapOf -> jsonObjectOf(
            "kind" to JsonValue.JsonString("map"),
            "inner" to toJson(type.inner, project, interner),
            "keyType" to toJson(type.keyType, project, interner)
        )
        ConfigFieldType.Unknown -> jsonObjectOf("kind" to JsonValue.JsonString("unknown"))
    }

    /** Interns [type]'s fields (and variants) under [alias], returning the alias to reference it by. */
    fun internNested(alias: String, type: ConfigFieldType.Nested, project: Project, interner: SchemaExportInterner): String {
        val variantsJson = if (type.variants.isEmpty()) null else JsonValue.JsonObject().also { obj ->
            for ((variant, fields) in type.variants) obj[variant] = fieldsToJson(fields, project, interner)
        }
        return interner.internNested(alias, fieldsToJson(type.fields, project, interner), type.discriminator, variantsJson)
    }

    fun fieldsToJson(fields: Map<String, ConfigFieldType>, project: Project, interner: SchemaExportInterner): JsonValue.JsonObject {
        val obj = JsonValue.JsonObject()
        for ((key, value) in fields) obj[key] = toJson(value, project, interner)
        return obj
    }
}
