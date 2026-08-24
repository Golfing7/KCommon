package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLSequence
import org.jetbrains.yaml.psi.YAMLSequenceItem

/**
 * Locates the module a YAML file (or, via `#@Module`/`#@File` tags, a section within one) belongs
 * to, and resolves the [ConfigFieldType] governing any given key. Everything here is deliberately
 * lenient - any failure to resolve just returns null/Indeterminate, meaning "don't annotate/complete
 * this", never an error.
 *
 * Three ways a module/bucket gets attached to a file or key, tried in this order:
 *  1. A `#$Type` tag directly on the key (see [FileTagParser.typeTag]) - short-circuits straight to
 *     a [NamedTypeRegistry] lookup, bypassing module resolution entirely.
 *  2. A `#@Module`/`#@File` tag on the key or an enclosing key (see [FileTagParser.sectionTag]) -
 *     the nearest one found scopes just that key's subtree to a (possibly different) module.
 *  3. A `#@Module`/`#@File` tag at the top of the file, or, failing that, the `<module-id>/config.yml`
 *     directory convention (see [resolveForFile]).
 *
 * Once a (module, bucket) pair is known, the schema itself is built from whichever source has it:
 * the project's own PSI (Java sources/classes) when available, otherwise anything imported into
 * [ExternalSchemaRegistry] - letting the same resolution logic serve both a full KCommon-consuming
 * project and a lone YAML file opened outside one (e.g. downloaded for remote/SFTP editing).
 */
object ConfigSchemaResolver {

    private const val MAX_RESOURCES_SEARCH_DEPTH = 6

    /** The schema governing some point in a file, plus the path *within that schema* left to resolve - not necessarily the file's own full key path, since a `#@Module`/`#@File` tag on an ancestor key re-roots it. */
    data class SchemaContext(val schema: ConfigSchema, val relativePath: List<String>)

    /** The outcome of resolving one YAMLKeyValue: its type (if any), and the module id that governed the resolution (for diagnostic messages). */
    data class KeyResolution(val moduleId: String?, val resolution: SchemaResolution)

    /** The known fields (or map key type) of some container - a YAMLMapping, or the value-position under a `#$Type`-tagged key. */
    data class ContainerResolution(val fields: FieldContainer?, val mapKeyType: ConfigFieldType.EnumLike?) {
        companion object {
            val EMPTY = ContainerResolution(null, null)
        }
    }

    fun resolveForFile(file: PsiFile): ConfigSchema? {
        return CachedValuesManager.getCachedValue(file) {
            CachedValueProvider.Result.create(
                computeSchema(file),
                PsiModificationTracker.MODIFICATION_COUNT,
                ExternalSchemaRegistry.getInstance(file.project).modificationTracker()
            )
        }
    }

    /**
     * Resolves [keyValue]'s type, honoring `#$Type` and `#@Module`/`#@File` tag overrides before
     * falling back to plain path resolution.
     *
     * [fallbackFile] is the real, disk-backed file to use for the final directory-convention/file-tag
     * fallback (see [resolveForFile]) - it defaults to [keyValue]'s own containing file, but a caller
     * working from completion's non-physical analysis copy (whose VirtualFile has no parent, so
     * directory-based resolution can't use it) should pass `parameters.originalFile` instead. Tag/path
     * resolution itself still walks [keyValue]'s own PSI, copy or not, since only structure matters there.
     */
    fun resolveKey(keyValue: YAMLKeyValue, fallbackFile: PsiFile = keyValue.containingFile): KeyResolution {
        val fullPath = buildKeyPath(keyValue) ?: return KeyResolution(null, SchemaResolution.Indeterminate)

        FileTagParser.typeTag(keyValue)?.let { typeName ->
            // No module context to report here: a `#$Type` override never yields UnknownKey (the
            // only resolution the annotator attaches a module id to), so resolving one would be wasted work.
            val type = NamedTypeRegistry.resolve(keyValue.project, typeName)
            return KeyResolution(null, if (type != null) SchemaResolution.Resolved(type) else SchemaResolution.Indeterminate)
        }

        val ctx = schemaContextFor(fallbackFile, keyValue, fullPath)
            ?: return KeyResolution(null, SchemaResolution.Indeterminate)
        return KeyResolution(ctx.schema.moduleId, ctx.schema.resolve(ctx.relativePath))
    }

    /** Resolves the fields/map-key-type available for suggestion inside [mapping] - honoring the same tag overrides as [resolveKey] (including the same [fallbackFile] caveat) when [mapping] is itself a `#$Type`-tagged key's value. */
    fun resolveContainer(mapping: YAMLMapping, fallbackFile: PsiFile = mapping.containingFile): ContainerResolution {
        val owner = mapping.parent as? YAMLKeyValue
        if (owner != null) {
            FileTagParser.typeTag(owner)?.let { typeName ->
                return when (val type = NamedTypeRegistry.resolve(owner.project, typeName)) {
                    is ConfigFieldType.Nested -> ContainerResolution(FieldContainer(type.typeName, type.fields), null)
                    is ConfigFieldType.MapOf -> ContainerResolution(null, type.keyType as? ConfigFieldType.EnumLike)
                    else -> ContainerResolution.EMPTY
                }
            }
        }

        val fullPath = buildContainerPath(mapping) ?: return ContainerResolution.EMPTY
        val ctx = schemaContextFor(fallbackFile, mapping.parent, fullPath) ?: return ContainerResolution.EMPTY
        val fields = ctx.schema.fieldsAt(ctx.relativePath)
        if (fields != null) return ContainerResolution(fields, null)
        return ContainerResolution(null, ctx.schema.mapKeyTypeAt(ctx.relativePath))
    }

    /**
     * Walks outward from [start] (a YAMLKeyValue or null) through its YAMLKeyValue ancestors,
     * looking for the nearest `#@Module`/`#@File` tag - see [FileTagParser.sectionTag]. When found
     * at ancestor depth `d` (0 = tag directly on [start] itself), the path relative to that new
     * schema's root is the last `d` segments of [fullPath] - everything *inside* the tagged key.
     * Falls back to [resolveForFile] (file-tag-or-directory-convention) when no section tag is found.
     */
    private fun schemaContextFor(file: PsiFile, start: PsiElement?, fullPath: List<String>): SchemaContext? {
        var node = start
        var idxFromEnd = 0
        while (node != null) {
            if (node is YAMLKeyValue) {
                val tag = FileTagParser.sectionTag(node)
                if (tag != null) {
                    val schema = buildSchema(file.project, tag.moduleId, tag.bucket ?: KCConstants.MAIN_CONFIG_BUCKET) ?: return null
                    val relative = fullPath.drop((fullPath.size - idxFromEnd).coerceAtLeast(0))
                    return SchemaContext(schema, relative)
                }
                idxFromEnd++
            }
            node = node.parent
        }
        val schema = resolveForFile(file) ?: return null
        return SchemaContext(schema, fullPath)
    }

    private fun computeSchema(file: PsiFile): ConfigSchema? {
        val fileTag = FileTagParser.fileTag(file)
        if (fileTag != null) {
            buildSchema(file.project, fileTag.moduleId, fileTag.bucket ?: KCConstants.MAIN_CONFIG_BUCKET)?.let { return it }
            // Tag present but unresolvable (e.g. typo'd module id) - fall through to directory
            // convention rather than giving up, per this plugin's usual leniency.
        }

        val vFile = file.virtualFile ?: return null
        if (!vFile.name.endsWith(".yml") && !vFile.name.endsWith(".yaml")) return null

        val parentDir = vFile.parent ?: return null
        val moduleId = parentDir.name

        var ancestor = parentDir.parent
        var depth = 0
        var underResources = false
        while (ancestor != null && depth < MAX_RESOURCES_SEARCH_DEPTH) {
            if (ancestor.name == "resources") {
                underResources = true
                break
            }
            ancestor = ancestor.parent
            depth++
        }
        if (!underResources) return null

        val bucket = vFile.name.substringBeforeLast('.')
        return buildSchema(file.project, moduleId, bucket)
    }

    /**
     * Builds the full schema for one (module, bucket) pair, trying the project's own PSI (Java
     * sources/classes) first, then falling back to anything imported into [ExternalSchemaRegistry]
     * for a project that doesn't have that module's classes on its classpath.
     */
    fun buildSchema(project: Project, moduleId: String, bucket: String): ConfigSchema? {
        val moduleClass = ConfigPsiUtil.findModuleClassByName(project, moduleId)
        if (moduleClass != null) {
            val moduleInfo = moduleClass.getAnnotation(KCConstants.MODULE_INFO)
            if (moduleInfo != null) {
                val configSources = ConfigPsiUtil.extractConfigSources(moduleInfo)
                return ConfigSchema(moduleId) { buildFieldsFromPsi(project, configSources, bucket) }
            }
        }

        val externalFields = ExternalSchemaRegistry.getInstance(project).fieldsFor(moduleId, bucket) ?: return null
        return ConfigSchema(moduleId) { externalFields }
    }

    /** The module's fields for [bucket], from its Java config sources plus any @MenuContainerInfo entries targeting the same bucket - shared by [buildSchema] and [SchemaExportAction]. */
    internal fun buildFieldsFromPsi(project: Project, configSources: List<PsiClass>, bucket: String): Map<String, ConfigFieldType> {
        val fields = LinkedHashMap<String, ConfigFieldType>()
        for (sourceClass in configSources) {
            fields.putAll(ConfigPsiUtil.collectModuleFields(sourceClass, bucket, project))
        }
        // @MenuContainerInfo entries aren't tied to a specific module (see MenuContainerRegistry's
        // doc comment) - merge them in for any module whose config bucket they target.
        for ((segments, leafType) in MenuContainerRegistry.forBucket(project, bucket)) {
            mergeField(fields, segments, leafType)
        }
        return fields
    }

    /** Merges [leafType] into [fields] at the given dot-path, synthesizing intermediate Nested wrappers as needed. */
    private fun mergeField(fields: MutableMap<String, ConfigFieldType>, segments: List<String>, leafType: ConfigFieldType) {
        val head = segments.first()
        if (segments.size == 1) {
            fields.putIfAbsent(head, leafType)
            return
        }

        val existing = fields[head] as? ConfigFieldType.Nested
        val childFields = LinkedHashMap(existing?.fields.orEmpty())
        mergeField(childFields, segments.drop(1), leafType)
        fields[head] = ConfigFieldType.Nested(existing?.typeName ?: "object", childFields)
    }

    /** Builds the dot-path of YAML keys from the document root down to [keyValue], ignoring sequence indices. */
    fun buildKeyPath(keyValue: YAMLKeyValue): List<String>? = buildPathUpFrom(keyValue)

    /**
     * Builds the dot-path of YAML keys from the document root down to (but not including) the
     * given [mapping] itself - i.e. the path to the container whose missing keys we'd suggest.
     */
    fun buildContainerPath(mapping: YAMLMapping): List<String>? = buildPathUpFrom(mapping.parent)

    private fun buildPathUpFrom(start: PsiElement?): List<String>? {
        val path = ArrayDeque<String>()
        var current: PsiElement? = start

        while (current != null) {
            when (current) {
                is YAMLKeyValue -> {
                    val key = current.keyText
                    if (key.isBlank()) return null
                    path.addFirst(key)
                    current = current.parent
                }
                is YAMLMapping -> current = current.parent
                is YAMLSequenceItem -> current = current.parent
                is YAMLSequence -> current = current.parent
                is YAMLDocument, is YAMLFile -> current = null
                else -> current = current.parent
            }
        }
        return path.toList()
    }
}
