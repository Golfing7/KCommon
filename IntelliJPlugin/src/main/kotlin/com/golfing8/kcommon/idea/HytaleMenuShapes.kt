package com.golfing8.kcommon.idea

import com.golfing8.kcommon.idea.ConfigFieldType.EnumLike
import com.golfing8.kcommon.idea.ConfigFieldType.Formatted
import com.golfing8.kcommon.idea.ConfigFieldType.MapOf
import com.golfing8.kcommon.idea.ConfigFieldType.Nested
import com.golfing8.kcommon.idea.ConfigFieldType.Unknown
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootModificationTracker
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

/**
 * The config shape of a Hytale `MenuBuilder`. The element whitelist comes from `kcommon/menu-schema.json`,
 * which the Hytale module writes from its own `MenuSchema` (a test there keeps it current): the copy
 * in the project's KCommon when there is one, otherwise the copy bundled with this plugin. The
 * menu's own options (title, width, ...) mirror `MenuBuilder(Map, String)`.
 *
 * Elements are a [Nested] type discriminated by `type:` (defaulting to Group), and nest through
 * `children`. The schema model is eager, so nesting stops after [MAX_DEPTH] levels; anything deeper
 * just isn't checked.
 */
object HytaleMenuShapes {
    const val MENU_TYPE_NAME = "HytaleMenu"
    private const val MAX_DEPTH = 10
    private const val SCHEMA_PATH = "kcommon/menu-schema.json"

    /** The whitelist as read from the JSON, with property names in markup form (`LayoutMode`). */
    class Schema(
        val containers: Set<String>,
        val textTypes: Set<String>,
        /** Element type -> markup property name -> its config type. */
        val elements: Map<String, Map<String, ConfigFieldType>>,
    ) {
        val menu: Nested by lazy { buildMenu(this) }

        /** The config keys (`layout-mode`) an element type takes, besides `type`, `special`, `text` and `children`. */
        fun propertyKeys(type: String): Set<String>? = elements[type]?.keys?.mapTo(LinkedHashSet()) { ConfigPsiUtil.camelToKebab(it) }
    }

    private val bundled: Schema by lazy {
        parse(HytaleMenuShapes::class.java.getResourceAsStream("/$SCHEMA_PATH")!!.use { it.readBytes().toString(Charsets.UTF_8) })
    }

    /** The menu shape for [project] (the bundled schema when it's null or has no KCommon copy of its own). */
    fun menu(project: Project?): Nested = schema(project).menu

    fun schema(project: Project?): Schema {
        if (project == null || project.isDefault) return bundled
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val file = FilenameIndex.getVirtualFilesByName("menu-schema.json", GlobalSearchScope.allScope(project))
                .firstOrNull { it.path.endsWith(SCHEMA_PATH) }
            val schema = file?.let { runCatching { parse(String(it.contentsToByteArray(), Charsets.UTF_8)) }.getOrNull() } ?: bundled
            CachedValueProvider.Result.create(
                schema,
                *listOfNotNull(ProjectRootModificationTracker.getInstance(project), VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS, file).toTypedArray()
            )
        }
    }

    internal fun parse(json: String): Schema {
        val root = MiniJson.parse(json) as JsonValue.JsonObject
        fun strings(key: String) = (root[key] as? JsonValue.JsonArray)?.items?.mapNotNull { (it as? JsonValue.JsonString)?.value }?.toSet().orEmpty()
        val elements = LinkedHashMap<String, Map<String, ConfigFieldType>>()
        for ((type, props) in (root["elements"] as JsonValue.JsonObject).entries) {
            elements[type] = properties(props as JsonValue.JsonObject)
        }
        return Schema(strings("containers"), strings("textTypes"), elements)
    }

    private fun properties(obj: JsonValue.JsonObject): Map<String, ConfigFieldType> =
        obj.entries.mapValuesTo(LinkedHashMap()) { (name, value) -> property(name, value as JsonValue.JsonObject) }

    private fun property(name: String, obj: JsonValue.JsonObject): ConfigFieldType {
        val uiType = (obj["uiType"] as? JsonValue.JsonString)?.value
        val keys = (obj["keys"] as? JsonValue.JsonObject)?.let(::properties).orEmpty().mapKeys { ConfigPsiUtil.camelToKebab(it.key) }
        return when ((obj["kind"] as? JsonValue.JsonString)?.value) {
            "BOOL" -> bool
            "INTEGER" -> Formatted(ScalarFormat.INTEGER)
            "DECIMAL" -> Formatted(ScalarFormat.DECIMAL)
            "COLOR" -> Formatted(ScalarFormat.COLOR)
            "ENUM" -> EnumLike(EnumSource.FixedValues((obj["values"] as? JsonValue.JsonArray)?.items?.mapNotNull { (it as? JsonValue.JsonString)?.value }.orEmpty()))
            // A tuple may also be a reference to a .ui value of its type.
            "TUPLE" -> Nested(uiType ?: name, keys, scalar = Formatted(ScalarFormat.REF, uiType))
            "PATCH" -> Nested(uiType ?: "PatchStyle", keys, scalar = Formatted(ScalarFormat.PATCH, uiType))
            // A reference, or `{ref: '@Style', sounds: '@Sounds'}`.
            "REF" -> Nested(
                uiType ?: "UiReference",
                mapOf("ref" to Formatted(ScalarFormat.REF, uiType), "sounds" to Formatted(ScalarFormat.REF)),
                scalar = Formatted(ScalarFormat.REF, uiType)
            )
            else -> if (name == "ItemId") EnumLike(EnumSource.HytaleItemIds) else Unknown
        }
    }

    private val bool = EnumLike(EnumSource.FixedValues(listOf("true", "false")))

    private fun buildMenu(schema: Schema): Nested {
        var level = MapOf(Unknown)
        repeat(MAX_DEPTH) { level = MapOf(element(schema, level)) }
        val group = schema.elements["Group"].orEmpty().mapKeys { ConfigPsiUtil.camelToKebab(it.key) }
        return Nested(
            MENU_TYPE_NAME,
            linkedMapOf(
                "title" to Unknown,
                "width" to Formatted(ScalarFormat.INTEGER),
                "height" to Formatted(ScalarFormat.INTEGER),
                "overlay" to bool,
                "frame" to enumOf("decorated", "none"),
                "lifetime" to enumOf("cant-close", "can-dismiss", "can-dismiss-or-close-through-interaction"),
                "elements-per-page" to Formatted(ScalarFormat.INTEGER),
                // The content Group's own properties; its elements go under `elements`.
                "content" to Nested("Group", group),
                "elements" to level,
                "templates" to level,
            )
        )
    }

    private fun element(schema: Schema, children: MapOf): Nested {
        val typeField = EnumLike(EnumSource.FixedValues(schema.elements.keys.toList()))
        val variants = LinkedHashMap<String, Map<String, ConfigFieldType>>()
        for ((type, own) in schema.elements) {
            val fields = LinkedHashMap<String, ConfigFieldType>()
            fields["type"] = typeField
            fields["special"] = bool
            if (type in schema.textTypes) fields["text"] = Unknown
            if (type in schema.containers) fields["children"] = children
            for ((name, propertyType) in own) fields[ConfigPsiUtil.camelToKebab(name)] = propertyType
            variants[type] = fields
        }
        return Nested("MenuElement", variants["Group"] ?: variants.values.first(), "type", variants)
    }

    private fun enumOf(vararg values: String) = EnumLike(EnumSource.FixedValues(values.toList()))
}
