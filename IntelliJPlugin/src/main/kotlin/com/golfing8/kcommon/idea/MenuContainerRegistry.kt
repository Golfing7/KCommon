package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiClassObjectAccessExpression
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
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

    /** Bucket key for a `getOrLoad` call whose section couldn't be traced to one config file. */
    private const val ANY_BUCKET = "*"

    /** The (path segments -> MenuBuilder-shaped field type) entries declared for the given config bucket. */
    fun forBucket(project: Project, bucket: String): List<Pair<List<String>, ConfigFieldType>> {
        val scanned = scan(project)
        return scanned[bucket].orEmpty() + scanned[ANY_BUCKET].orEmpty()
    }

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

            val withLoads = LinkedHashMap<String, MutableList<Pair<List<String>, ConfigFieldType>>>()
            result.forEach { (bucket, entries) -> withLoads[bucket] = entries.toMutableList() }
            scanGetOrLoad(project, withLoads)

            CachedValueProvider.Result.create(withLoads, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }

    /**
     * `section.getOrLoad("shop-menu", MenuBuilder.class)` calls, the way both platforms load menus by
     * hand (on Hytale usually from `getConfig("menus")`). The bucket comes from a `getConfig("...")` or
     * `getMainConfig()` qualifier; any other qualifier applies the path to every bucket.
     */
    private fun scanGetOrLoad(project: Project, into: MutableMap<String, MutableList<Pair<List<String>, ConfigFieldType>>>) {
        val scope = GlobalSearchScope.allScope(project)
        val facade = JavaPsiFacade.getInstance(project)
        val section = facade.findClass(KCConstants.KSECTION, scope) ?: return
        val getOrLoad = section.findMethodsByName("getOrLoad", false).firstOrNull {
            it.parameterList.parametersCount == 2 && (it.parameterList.parameters[1].type as? com.intellij.psi.PsiClassType)?.className == "Class"
        } ?: return
        for (reference in MethodReferencesSearch.search(getOrLoad, scope, true)) {
            val call = PsiTreeUtil.getParentOfType(reference.element, PsiMethodCallExpression::class.java) ?: continue
            val args = call.argumentList.expressions
            if (args.size != 2) continue
            val loaded = (args[1] as? PsiClassObjectAccessExpression)?.operand?.type?.let(PsiTypesUtil::getPsiClass)
            if (loaded?.qualifiedName != KCConstants.MENU_BUILDER) continue
            val path = facade.constantEvaluationHelper.computeConstantExpression(args[0]) as? String ?: continue
            val segments = path.split('.').map(String::trim).filter(String::isNotEmpty)
            if (segments.isEmpty()) continue
            val bucket = bucketOf(project, call.methodExpression.qualifierExpression) ?: ANY_BUCKET
            val leaf = if (PlatformDetector.isHytale(call)) {
                HytaleMenuShapes.menu(project)
            } else {
                ConfigFieldType.Nested("MenuBuilder", MenuBuilderShapes.menuBuilderFields(MenuContainerKind.NORMAL))
            }
            into.getOrPut(bucket) { mutableListOf() }.add(segments to leaf)
        }
    }

    internal fun bucketOf(project: Project, qualifier: PsiExpression?): String? {
        val call = qualifier as? PsiMethodCallExpression ?: return null
        return when (call.methodExpression.referenceName) {
            "getMainConfig" -> KCConstants.MAIN_CONFIG_BUCKET
            "getConfig" -> call.argumentList.expressions.singleOrNull()?.let {
                JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(it) as? String
            }
            else -> null
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
