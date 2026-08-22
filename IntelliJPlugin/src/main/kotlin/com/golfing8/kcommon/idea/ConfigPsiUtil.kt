package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTypesUtil

/** Reflection-mirroring PSI helpers: reads @ModuleInfo / @Conf / CASerializable the same way KCommon's runtime does. */
object ConfigPsiUtil {

    fun findModuleClassByName(project: Project, moduleId: String): PsiClass? {
        val annotationClass = JavaPsiFacade.getInstance(project)
            .findClass(KCConstants.MODULE_INFO, GlobalSearchScope.allScope(project)) ?: return null

        val scope = GlobalSearchScope.allScope(project)
        for (candidate in AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope)) {
            val annotation = candidate.getAnnotation(KCConstants.MODULE_INFO) ?: continue
            val nameValue = annotation.findAttributeValue("name") ?: continue
            val constant = JavaPsiFacade.getInstance(project).constantEvaluationHelper
                .computeConstantExpression(nameValue) as? String ?: continue
            if (constant.equals(moduleId, ignoreCase = true)) return candidate
        }
        return null
    }

    fun extractConfigSources(annotation: PsiAnnotation): List<PsiClass> {
        val value = annotation.findAttributeValue("configSources") ?: return emptyList()
        val values = if (value is PsiArrayInitializerMemberValue) value.initializers.toList() else listOf(value)
        return values.mapNotNull { v ->
            (v as? PsiClassObjectAccessExpression)?.operand?.type?.let { PsiTypesUtil.getPsiClass(it) }
        }
    }

    /** Fields of a module-level ConfigClassSource that belong to the given config file bucket (e.g. "config", "limits"). */
    fun collectModuleFields(psiClass: PsiClass, bucket: String, project: Project): Map<String, ConfigFieldType> {
        val result = LinkedHashMap<String, ConfigFieldType>()
        for (field in psiClass.fields) {
            val conf = field.getAnnotation(KCConstants.CONF) ?: continue
            val configAttr = stringLiteralValue(conf.findAttributeValue("config"))
            val fieldBucket = if (configAttr.isNullOrBlank() || configAttr == KCConstants.DEFAULT_CONFIG_BUCKET) KCConstants.MAIN_CONFIG_BUCKET else configAttr
            if (!fieldBucket.equals(bucket, ignoreCase = true)) continue

            result[yamlKeyFor(field, conf)] = classifyType(field.type, project)
        }
        return result
    }

    /** Fields of a nested CASerializable type - no bucket filtering, that only applies at the module's top level. */
    fun collectConfFields(psiClass: PsiClass): Map<String, ConfigFieldType> {
        return CachedValuesManager.getCachedValue(psiClass) {
            val project = psiClass.project
            val result = LinkedHashMap<String, ConfigFieldType>()
            for (field in psiClass.fields) {
                val conf = field.getAnnotation(KCConstants.CONF) ?: continue
                result[yamlKeyFor(field, conf)] = classifyType(field.type, project)
            }
            CachedValueProvider.Result.create(
                result as Map<String, ConfigFieldType>,
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

    private fun yamlKeyFor(field: PsiField, conf: PsiAnnotation): String {
        val label = stringLiteralValue(conf.findAttributeValue("label"))
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
                val valueParam = type.parameters.getOrNull(1)
                val inner = valueParam?.let { classifyType(it, project) } ?: ConfigFieldType.Unknown
                return ConfigFieldType.MapOf(inner)
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
