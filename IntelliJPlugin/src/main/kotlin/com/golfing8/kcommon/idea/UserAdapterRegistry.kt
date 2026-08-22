package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiArrayInitializerMemberValue
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTypesUtil

/**
 * Discovers ConfigAdapter implementations that a KCommon-consuming project has annotated with
 * @ConfigAdapterInfo, and models their declared shape the same way [BuiltInAdapters] does for
 * KCommon's own types - letting a project register its own serializable config types without
 * this plugin needing to know about them ahead of time.
 */
object UserAdapterRegistry {

    private const val CONFIG_ADAPTER_INFO = "com.golfing8.kcommon.config.adapter.ConfigAdapterInfo"
    private const val CONFIG_ADAPTER = "com.golfing8.kcommon.config.adapter.ConfigAdapter"

    fun forType(project: Project, qualifiedName: String): ConfigFieldType? = scan(project)[qualifiedName]

    private fun scan(project: Project): Map<String, ConfigFieldType> {
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val infoAnnotationClass = JavaPsiFacade.getInstance(project)
                .findClass(CONFIG_ADAPTER_INFO, GlobalSearchScope.allScope(project))

            val result = if (infoAnnotationClass == null) {
                emptyMap()
            } else {
                val scope = GlobalSearchScope.allScope(project)
                val entries = LinkedHashMap<String, ConfigFieldType>()
                for (candidate in AnnotatedElementsSearch.searchPsiClasses(infoAnnotationClass, scope)) {
                    if (!InheritanceUtil.isInheritor(candidate, CONFIG_ADAPTER)) continue

                    val annotation = candidate.getAnnotation(CONFIG_ADAPTER_INFO) ?: continue
                    val adaptedType = extractAdaptedType(annotation) ?: continue
                    val type = buildFieldType(annotation, adaptedType) ?: continue
                    entries[adaptedType.qualifiedName ?: continue] = type
                }
                entries
            }

            CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    private fun extractAdaptedType(annotation: PsiAnnotation): PsiClass? {
        val value = annotation.findAttributeValue("value") ?: return null
        return (value as? PsiClassObjectAccessExpression)?.operand?.type?.let { PsiTypesUtil.getPsiClass(it) }
    }

    /** Returns null when the annotation declares no override (Shape.UNKNOWN), so structural detection still applies. */
    private fun buildFieldType(annotation: PsiAnnotation, adaptedType: PsiClass): ConfigFieldType? {
        val shapeValue = annotation.findAttributeValue("shape")
        val shapeName = ((shapeValue as? PsiReferenceExpression)?.resolve() as? PsiEnumConstant)?.name

        return when (shapeName) {
            "ENUM" -> ConfigFieldType.EnumLike(enumSourceFor(annotation, adaptedType))
            "OBJECT" -> ConfigFieldType.Nested(
                adaptedType.name ?: "object",
                stringArrayAttribute(annotation, "keys").associateWith { ConfigFieldType.Unknown }
            )
            else -> null
        }
    }

    private fun enumSourceFor(annotation: PsiAnnotation, adaptedType: PsiClass): EnumSource {
        val explicitValues = stringArrayAttribute(annotation, "enumValues")
        if (explicitValues.isNotEmpty()) return EnumSource.FixedValues(explicitValues)

        val qualifiedName = adaptedType.qualifiedName.orEmpty()
        return if (adaptedType.isEnum) EnumSource.JavaEnum(qualifiedName) else EnumSource.StaticFieldsOfOwnType(qualifiedName)
    }

    private fun stringArrayAttribute(annotation: PsiAnnotation, name: String): List<String> {
        val value = annotation.findAttributeValue(name) ?: return emptyList()
        val values = if (value is PsiArrayInitializerMemberValue) value.initializers.toList() else listOf(value)
        return values.mapNotNull { (it as? PsiLiteralExpression)?.value as? String }
    }
}
