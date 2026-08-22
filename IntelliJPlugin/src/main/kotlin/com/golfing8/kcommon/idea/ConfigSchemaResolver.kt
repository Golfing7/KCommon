package com.golfing8.kcommon.idea

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
 * Locates the module a YAML file belongs to, by convention: `.../resources/<module-id>/<file>.yml`.
 * Everything here is deliberately lenient - any failure to resolve just returns null, meaning
 * "don't annotate/complete this file", never an error.
 */
object ConfigSchemaResolver {

    private const val MAX_RESOURCES_SEARCH_DEPTH = 6

    fun resolveForFile(file: PsiFile): ConfigSchema? {
        return CachedValuesManager.getCachedValue(file) {
            CachedValueProvider.Result.create(computeSchema(file), PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    private fun computeSchema(file: PsiFile): ConfigSchema? {
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
        val project = file.project

        val moduleClass = ConfigPsiUtil.findModuleClassByName(project, moduleId) ?: return null
        val moduleInfo = moduleClass.getAnnotation(KCConstants.MODULE_INFO) ?: return null
        val configSources = ConfigPsiUtil.extractConfigSources(moduleInfo)

        return ConfigSchema(moduleId) {
            val fields = LinkedHashMap<String, ConfigFieldType>()
            for (sourceClass in configSources) {
                fields.putAll(ConfigPsiUtil.collectModuleFields(sourceClass, bucket, project))
            }
            // @MenuContainerInfo entries aren't tied to a specific module (see MenuContainerRegistry's
            // doc comment) - merge them in for any module whose config bucket they target.
            for ((segments, leafType) in MenuContainerRegistry.forBucket(project, bucket)) {
                mergeField(fields, segments, leafType)
            }
            fields
        }
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
