package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTypesUtil

/** Reflection-mirroring PSI helpers: reads @ModuleInfo / @Conf / CASerializable the same way KCommon's runtime does. */
object ConfigPsiUtil {

    private val MODULES_BY_NAME_KEY: Key<CachedValue<Map<String, PsiClass>>> = Key.create("kcommon.modulesByName")
    private val CA_SERIALIZABLE_CLASSES_KEY: Key<CachedValue<Map<String, PsiClass>>> = Key.create("kcommon.caSerializableClasses")

    fun findModuleClassByName(project: Project, moduleId: String): PsiClass? =
        allModulesByName(project).entries.firstOrNull { it.key.equals(moduleId, ignoreCase = true) }?.value

    /** Every @ModuleInfo-annotated class in the project, keyed by its declared (original-case) module id - e.g. for [SchemaExportAction] to enumerate every module, or for tag completion to suggest module ids. */
    fun allModulesByName(project: Project): Map<String, PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(project, MODULES_BY_NAME_KEY, {
            val annotationClass = JavaPsiFacade.getInstance(project)
                .findClass(KCConstants.MODULE_INFO, GlobalSearchScope.allScope(project))

            val result: Map<String, PsiClass> = if (annotationClass == null) {
                emptyMap()
            } else {
                val scope = GlobalSearchScope.allScope(project)
                val map = LinkedHashMap<String, PsiClass>()
                for (candidate in AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope)) {
                    val annotation = candidate.getAnnotation(KCConstants.MODULE_INFO) ?: continue
                    val nameValue = annotation.findAttributeValue("name") ?: continue
                    val constant = JavaPsiFacade.getInstance(project).constantEvaluationHelper
                        .computeConstantExpression(nameValue) as? String ?: continue
                    map[constant] = candidate
                }
                map
            }

            CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
    }

    /** Every config bucket name referenced by [configSources]' @Conf-annotated static fields, always including the default "config" bucket - for [SchemaExportAction] to know which buckets to export per module. */
    fun collectBuckets(configSources: List<PsiClass>): Set<String> {
        val buckets = linkedSetOf(KCConstants.MAIN_CONFIG_BUCKET)
        for (source in configSources) {
            for (field in source.fields) {
                if (!shouldSerializeModuleField(field)) continue
                val conf = field.getAnnotation(KCConstants.CONF) ?: continue
                val configAttr = stringLiteralValue(conf.findAttributeValue("config"))
                buckets += if (configAttr.isNullOrBlank() || configAttr == KCConstants.DEFAULT_CONFIG_BUCKET) {
                    KCConstants.MAIN_CONFIG_BUCKET
                } else {
                    configAttr
                }
            }
        }
        return buckets
    }

    /** A CASerializable class anywhere on the project's classpath, found by its simple name - for `#$Type` tag resolution (see [NamedTypeRegistry]). Null for a flattened type (see [isFlattened]), since those have no fixed key set to offer. */
    fun findNamedCASerializable(project: Project, name: String): ConfigFieldType.Nested? {
        val match = allCASerializableClasses(project)[name] ?: return null
        if (isFlattened(match)) return null
        return ConfigFieldType.Nested(match.name ?: name, collectConfFields(match))
    }

    fun allCASerializableTypeNames(project: Project): List<String> = allCASerializableClasses(project).keys.toList()

    private fun allCASerializableClasses(project: Project): Map<String, PsiClass> {
        return CachedValuesManager.getManager(project).getCachedValue(project, CA_SERIALIZABLE_CLASSES_KEY, {
            val caClass = JavaPsiFacade.getInstance(project)
                .findClass(KCConstants.CA_SERIALIZABLE, GlobalSearchScope.allScope(project))

            val result: Map<String, PsiClass> = if (caClass == null) {
                emptyMap()
            } else {
                val map = LinkedHashMap<String, PsiClass>()
                for (candidate in ClassInheritorsSearch.search(caClass, GlobalSearchScope.allScope(project), true)) {
                    val name = candidate.name ?: continue
                    map.putIfAbsent(name, candidate)
                }
                map
            }

            CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
    }

    fun extractConfigSources(annotation: PsiAnnotation): List<PsiClass> {
        val value = annotation.findAttributeValue("configSources") ?: return emptyList()
        val values = if (value is PsiArrayInitializerMemberValue) value.initializers.toList() else listOf(value)
        return values.mapNotNull { v ->
            (v as? PsiClassObjectAccessExpression)?.operand?.type?.let { PsiTypesUtil.getPsiClass(it) }
        }
    }

    /** Mirrors FieldHandle#shouldSerialize: only non-static/non-transient fields (and never the reserved _key field) are serialized. */
    private fun shouldSerialize(field: PsiField): Boolean {
        if (field.name == KCConstants.KEY_FIELD_NAME) return false
        val modifiers = field.modifierList ?: return true
        return !modifiers.hasModifierProperty(PsiModifier.STATIC) && !modifiers.hasModifierProperty(PsiModifier.TRANSIENT)
    }

    /** ConfigClassSource fields are serialized only if they're static and annotated with @Conf. */
    private fun shouldSerializeModuleField(field: PsiField): Boolean {
        val modifiers = field.modifierList ?: return false
        if (!modifiers.hasModifierProperty(PsiModifier.STATIC)) return false
        return field.getAnnotation(KCConstants.CONF) != null
    }

    /** Fields of a module-level ConfigClassSource that belong to the given config file bucket (e.g. "config", "limits"). */
    fun collectModuleFields(psiClass: PsiClass, bucket: String, project: Project): Map<String, ConfigFieldType> {
        val result = LinkedHashMap<String, ConfigFieldType>()
        for (field in psiClass.fields) {
            if (!shouldSerializeModuleField(field)) continue
            val conf = field.getAnnotation(KCConstants.CONF)
            val configAttr = stringLiteralValue(conf?.findAttributeValue("config"))
            val fieldBucket = if (configAttr.isNullOrBlank() || configAttr == KCConstants.DEFAULT_CONFIG_BUCKET) KCConstants.MAIN_CONFIG_BUCKET else configAttr
            if (!fieldBucket.equals(bucket, ignoreCase = true)) continue

            result[yamlKeyFor(field, conf)] = classifyType(field.type, project)
        }
        return result
    }

    /** The CASerializable classes currently being expanded on this thread, to break cycles in [collectConfFields] - see its doc. */
    private val expansionStack = ThreadLocal.withInitial { mutableSetOf<PsiClass>() }

    /**
     * Fields of a nested CASerializable type - no bucket filtering, that only applies at the module's top level.
     *
     * A CASerializable type can reference itself, directly or through a cycle of other CASerializable
     * types (e.g. a "condition" type with a `sub-conditions: List<Condition>` field) - naively
     * recursing through [classifyType] for such a field would call back into this same function for
     * the same [psiClass] before its first call ever returns, overflowing the stack. [expansionStack]
     * detects that reentrancy and, per this plugin's usual leniency, just leaves the field that closes
     * the loop as an empty object (unvalidated) instead of expanding it forever.
     */
    fun collectConfFields(psiClass: PsiClass): Map<String, ConfigFieldType> {
        val stack = expansionStack.get()
        if (!stack.add(psiClass)) return emptyMap()
        try {
            return CachedValuesManager.getCachedValue(psiClass) {
                val project = psiClass.project
                val result = LinkedHashMap<String, ConfigFieldType>()
                for (field in psiClass.fields) {
                    if (!shouldSerialize(field)) continue
                    val conf = field.getAnnotation(KCConstants.CONF)
                    result[yamlKeyFor(field, conf)] = classifyType(field.type, project)
                }
                CachedValueProvider.Result.create(
                    result as Map<String, ConfigFieldType>,
                    PsiModificationTracker.MODIFICATION_COUNT
                )
            }
        } finally {
            stack.remove(psiClass)
        }
    }

    private fun yamlKeyFor(field: PsiField, conf: PsiAnnotation?): String {
        val label = stringLiteralValue(conf?.findAttributeValue("label"))
        return if (!label.isNullOrBlank()) label else camelToKebab(field.name)
    }

    private fun stringLiteralValue(value: PsiAnnotationMemberValue?): String? =
        (value as? PsiLiteralExpression)?.value as? String

    /** Mirrors KCommon's own StringUtil.camelToYaml: a dash only on a lower->upper transition. */
    fun camelToKebab(input: String): String {
        val sb = StringBuilder()
        var lastLower = false
        for (c in input) {
            if (c.isUpperCase() && lastLower) {
                lastLower = false
                sb.append('-').append(c.lowercaseChar())
            } else {
                lastLower = c.isLowerCase()
                sb.append(c.lowercaseChar())
            }
        }
        return sb.toString()
    }

    fun classifyType(type: PsiType, project: Project): ConfigFieldType {
        val psiClass = PsiTypesUtil.getPsiClass(type)

        if (type is PsiClassType && psiClass != null) {
            val qualifiedName = psiClass.qualifiedName

            // Optional<T> is transparent to the YAML shape - it's whatever T is.
            if (qualifiedName == KCConstants.OPTIONAL) {
                val inner = type.parameters.getOrNull(0)
                return inner?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
            }

            // Known-by-FQN types take priority: KCommon's own baked-in adapters, then anything a
            // consuming project has declared via @ConfigAdapterInfo.
            if (qualifiedName != null) {
                BuiltInAdapters.forType(qualifiedName)?.let { return it }
                UserAdapterRegistry.forType(project, qualifiedName)?.let { return it }
            }

            if (qualifiedName == KCConstants.RANGE_MAP || InheritanceUtil.isInheritor(psiClass, KCConstants.RANGE_MAP)) {
                val valueParam = type.parameters.getOrNull(0)
                val inner = valueParam?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                return ConfigFieldType.MapOf(inner)
            }

            if (qualifiedName == "java.util.Map" || InheritanceUtil.isInheritor(psiClass, "java.util.Map")) {
                val keyParam = type.parameters.getOrNull(0)
                val valueParam = type.parameters.getOrNull(1)
                val keyType = keyParam?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                val inner = valueParam?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                return ConfigFieldType.MapOf(inner, keyType)
            }

            if (isCollectionType(qualifiedName, psiClass)) {
                val param = type.parameters.getOrNull(0)
                val inner = param?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                return ConfigFieldType.ListOf(inner)
            }

            if (psiClass.isEnum) {
                return ConfigFieldType.EnumLike(EnumSource.JavaEnum(qualifiedName ?: return ConfigFieldType.Unknown))
            }

            if (InheritanceUtil.isInheritor(psiClass, KCConstants.DYNAMIC_ENUM)) {
                return ConfigFieldType.EnumLike(EnumSource.StaticFieldsOfOwnType(qualifiedName ?: return ConfigFieldType.Unknown))
            }

            if (InheritanceUtil.isInheritor(psiClass, KCConstants.CA_SERIALIZABLE) && !isFlattened(psiClass)) {
                return ConfigFieldType.Nested(psiClass.name ?: "object", collectConfFields(psiClass))
            }
        }

        return ConfigFieldType.Unknown
    }

    private fun isCollectionType(qualifiedName: String?, psiClass: PsiClass): Boolean {
        if (qualifiedName == "java.util.List" || qualifiedName == "java.util.Set" || qualifiedName == "java.util.Collection") {
            return true
        }
        return InheritanceUtil.isInheritor(psiClass, "java.util.Collection")
    }

    /**
     * A CASerializable with @CASerializable.Options(flatten = true) collapses to a bare scalar
     * in YAML rather than a mapping, so we must not treat it as a Nested object with validated keys.
     */
    private fun isFlattened(psiClass: PsiClass): Boolean {
        val options = psiClass.getAnnotation(KCConstants.CA_SERIALIZABLE_OPTIONS) ?: return false
        val flatten = options.findAttributeValue("flatten") ?: return false
        return (flatten as? PsiLiteralExpression)?.value as? Boolean ?: false
    }
}
