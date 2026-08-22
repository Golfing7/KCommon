package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.InheritanceUtil
import com.intellij.psi.util.PsiModificationTracker

/**
 * Discovers @MenuContainerInfo-annotated MenuContainer subclasses in a KCommon-consuming project,
 * and maps each one's declared `path` onto a MenuBuilder-shaped schema entry for the config
 * bucket its `config` targets.
 *
 * There's no static link from a MenuContainer back to a specific module - its config section is
 * whatever ConfigurationSection the container's own loadMenu() is handed at runtime - so entries
 * here are applied to every module's schema for the matching bucket, not just one. Worst case,
 * a module that doesn't actually use a given container silently accepts/completes that key too;
 * that's a false negative, never a false positive, matching the rest of this plugin's leniency.
 */
object MenuContainerRegistry {

    /** The (path segments -> MenuBuilder-shaped field type) entries declared for the given config bucket. */
    fun forBucket(project: Project, bucket: String): List<Pair<List<String>, ConfigFieldType>> =
        scan(project)[bucket].orEmpty()

    private fun scan(project: Project): Map<String, List<Pair<List<String>, ConfigFieldType>>> {
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val infoAnnotationClass = JavaPsiFacade.getInstance(project)
                .findClass(KCConstants.MENU_CONTAINER_INFO, GlobalSearchScope.allScope(project))

            val result = if (infoAnnotationClass == null) {
                emptyMap()
            } else {
                val scope = GlobalSearchScope.allScope(project)
                val byBucket = LinkedHashMap<String, MutableList<Pair<List<String>, ConfigFieldType>>>()

                for (candidate in AnnotatedElementsSearch.searchPsiClasses(infoAnnotationClass, scope)) {
                    if (!InheritanceUtil.isInheritor(candidate, KCConstants.MENU_CONTAINER)) continue
                    val annotation = candidate.getAnnotation(KCConstants.MENU_CONTAINER_INFO) ?: continue

                    val path = constantStringAttribute(project, annotation, "path") ?: continue
                    val segments = path.split('.').map(String::trim).filter(String::isNotEmpty)
                    if (segments.isEmpty()) continue

                    val configAttr = constantStringAttribute(project, annotation, "config")
                    val bucket = if (configAttr.isNullOrBlank() || configAttr == "@default") {
                        KCConstants.MAIN_CONFIG_BUCKET
                    } else {
                        configAttr
                    }

                    val kind = if (enumAttributeName(annotation, "type") == "PAGED") {
                        MenuContainerKind.PAGED
                    } else {
                        MenuContainerKind.NORMAL
                    }

                    val leaf = ConfigFieldType.Nested("MenuBuilder", MenuBuilderShapes.menuBuilderFields(kind))
                    byBucket.getOrPut(bucket) { mutableListOf() }.add(segments to leaf)
                }

                byBucket
            }

            CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    private fun constantStringAttribute(project: Project, annotation: PsiAnnotation, name: String): String? {
        val value = annotation.findAttributeValue(name) ?: return null
        return JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(value) as? String
    }

    private fun enumAttributeName(annotation: PsiAnnotation, name: String): String? {
        val value = annotation.findAttributeValue(name) ?: return null
        return ((value as? PsiReferenceExpression)?.resolve() as? PsiEnumConstant)?.name
    }
}
