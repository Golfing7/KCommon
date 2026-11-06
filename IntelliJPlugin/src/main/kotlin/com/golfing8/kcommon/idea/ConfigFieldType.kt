package com.golfing8.kcommon.idea

/**
 * The plugin's best guess at how a single config field is shaped, derived from the declared Java
 * field type - either via a hardcoded model of a known ConfigAdapter's shape (built-in KCommon
 * types, or a user's @ConfigAdapterInfo-annotated adapter), or, failing that, structurally from
 * the type itself (real enums, CASerializable, Collection/Map).
 */
sealed class ConfigFieldType {
    /** A fixed set of valid string values - see [EnumSource] for where they come from. */
    data class EnumLike(val source: EnumSource) : ConfigFieldType()

    /**
     * A YAML mapping with a fixed, known set of keys. [typeName] is shown to the user as where the keys come from.
     *
     * When [discriminator] is set, the mapping's own value under that key (such as `type: Label`)
     * picks which entry of [variants] holds its keys; [fields] is used when the value is missing or
     * unknown. Hytale menu elements and `@CASerializable.Options(typeResolverEnum = ...)` types are shaped like this.
     */
    data class Nested(
        val typeName: String,
        val fields: Map<String, ConfigFieldType>,
        val discriminator: String? = null,
        val variants: Map<String, Map<String, ConfigFieldType>> = emptyMap(),
    ) : ConfigFieldType() {
        fun fieldsFor(variant: String?): Map<String, ConfigFieldType> {
            if (variant == null || variants.isEmpty()) return fields
            return variants.entries.firstOrNull { it.key.equals(variant.trim(), ignoreCase = true) }?.value ?: fields
        }

        /** Shallow on purpose: a menu element nests copies of itself ten levels deep, so the generated hashCode would walk all of them. */
        override fun hashCode(): Int = typeName.hashCode() * 31 + fields.keys.hashCode()
    }

    /** List/Set/Collection<T>: a YAML sequence whose items should each match `inner`. */
    data class ListOf(val inner: ConfigFieldType) : ConfigFieldType()

    /** Map<K, T>: a YAML mapping with values matching `inner`. Keys match `keyType` (usually Unknown/free-form, but EnumLike for e.g. Map<XEnchantment, Integer>). */
    data class MapOf(val inner: ConfigFieldType, val keyType: ConfigFieldType = Unknown) : ConfigFieldType()

    /** Anything we don't have a specific model for (String, primitives, TimeLength, etc.) - never validated. */
    object Unknown : ConfigFieldType()
}

/** Result of walking a YAML key path against a resolved module's config schema. */
sealed class SchemaResolution {
    data class Resolved(val type: ConfigFieldType) : SchemaResolution()

    /**
     * The key at this path segment doesn't exist among the known fields of its parent.
     * [parentConfident] is true only when the parent's field set was fully known (a Nested
     * config object), as opposed to a free-form Map key, so it's safe to flag.
     */
    data class UnknownKey(val parentConfident: Boolean) : SchemaResolution()

    /** We lost track of the schema before reaching the end of the path (e.g. inside an Unknown type). */
    object Indeterminate : SchemaResolution()
}

/**
 * Reads the value a mapping holds under a key, for picking a [ConfigFieldType.Nested] variant.
 * The index is the position in the key path whose mapping is asked about.
 */
fun interface VariantLookup {
    fun valueAt(index: Int, key: String): String?

    companion object {
        val NONE = VariantLookup { _, _ -> null }
    }
}

/** The known keys available at some container (the config root, or a Nested field's own fields), plus a display name. */
data class FieldContainer(val typeName: String, val fields: Map<String, ConfigFieldType>)

/**
 * The resolved field structure for one config file (e.g. `<module>/config.yml`) belonging to one module.
 */
class ConfigSchema(val moduleId: String, rootFieldsProvider: () -> Map<String, ConfigFieldType>) {
    private val rootFields: Map<String, ConfigFieldType> by lazy(rootFieldsProvider)

    /**
     * The known keys at [containerPath] (empty for the config file's own root), for suggesting
     * missing keys. Returns null when the container isn't confidently known (e.g. free-form Map
     * keys, or an unresolved/indeterminate path).
     */
    fun fieldsAt(containerPath: List<String>, variants: VariantLookup = VariantLookup.NONE): FieldContainer? {
        if (containerPath.isEmpty()) {
            return FieldContainer("module '$moduleId' config", rootFields)
        }
        val resolution = resolve(containerPath, variants)
        val type = (resolution as? SchemaResolution.Resolved)?.type
        val nested = (type as? ConfigFieldType.ListOf)?.inner as? ConfigFieldType.Nested ?: type as? ConfigFieldType.Nested ?: return null
        return FieldContainer(nested.typeName, nested.fieldsFor(nested.discriminator?.let { variants.valueAt(containerPath.lastIndex, it) }))
    }

    /**
     * The key type of the MapOf at [containerPath] (empty means "not inside a container we can
     * say anything about"), for suggesting enum-like map keys. Null when the container isn't a
     * MapOf with a known key type.
     */
    fun mapKeyTypeAt(containerPath: List<String>, variants: VariantLookup = VariantLookup.NONE): ConfigFieldType.EnumLike? {
        if (containerPath.isEmpty()) return null
        val resolution = resolve(containerPath, variants)
        val mapOf = (resolution as? SchemaResolution.Resolved)?.type as? ConfigFieldType.MapOf ?: return null
        return mapOf.keyType as? ConfigFieldType.EnumLike
    }

    fun resolve(path: List<String>, variants: VariantLookup = VariantLookup.NONE): SchemaResolution {
        if (path.isEmpty()) return SchemaResolution.Indeterminate

        var knownFields: Map<String, ConfigFieldType>? = rootFields
        var freeFormInner: ConfigFieldType? = null
        var currentType: ConfigFieldType

        for ((idx, segment) in path.withIndex()) {
            val isLast = idx == path.lastIndex

            currentType = when {
                knownFields != null -> {
                    val match = knownFields.entries.firstOrNull { it.key.equals(segment, ignoreCase = true) }
                        ?: return SchemaResolution.UnknownKey(parentConfident = true)
                    match.value
                }
                freeFormInner != null -> freeFormInner
                else -> return SchemaResolution.Indeterminate
            }

            if (isLast) return SchemaResolution.Resolved(currentType)

            knownFields = null
            freeFormInner = null
            when (val type = currentType) {
                is ConfigFieldType.Nested -> knownFields = type.fieldsFor(type.discriminator?.let { variants.valueAt(idx, it) })
                is ConfigFieldType.MapOf -> freeFormInner = type.inner
                is ConfigFieldType.ListOf -> {
                    val inner = type.inner
                    if (inner is ConfigFieldType.Nested) {
                        knownFields = inner.fieldsFor(inner.discriminator?.let { variants.valueAt(idx, it) })
                    }
                    // else: can't say anything about what's inside a list of scalars/unknowns - stays indeterminate.
                }
                else -> Unit // EnumLike/Unknown with more path left - indeterminate deeper.
            }
        }
        return SchemaResolution.Indeterminate
    }
}
