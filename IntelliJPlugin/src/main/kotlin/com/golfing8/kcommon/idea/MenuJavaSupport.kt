package com.golfing8.kcommon.idea

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.patterns.PsiJavaPatterns
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiLocalVariable
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.ResolveResult
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.util.PsiUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.YAMLUtil
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * Links Java calls on a Hytale `MenuBuilder`/`MenuRow` to the YAML elements they name. `MenuScope`
 * throws on a key its menu doesn't have, so a typo in `bindTo("buy")` otherwise only shows up when a
 * player opens the menu.
 *
 * A builder is traced back through locals, fields, `orElseThrow()` and chained calls to the
 * `getOrLoad("shop-menu", MenuBuilder.class)` that loaded it, and the menu is looked up in the
 * project's YAML files at that path (in `<bucket>.yml` when the section came from `getConfig("<bucket>")`).
 */
object MenuJavaSupport {

    /** Where a builder or row came from. [template] is set for a row added with `addRow(container, template)`. */
    data class Target(val path: List<String>, val bucket: String?, val template: String? = null)

    /** A key argument of a menu call: which call, which argument, and what it names. */
    data class KeyArgument(val call: PsiMethodCallExpression, val method: String, val index: Int, val target: Target)

    /** Methods whose first argument is an element key. */
    private val KEY_METHODS = setOf("bindTo", "show", "specialPlaceholders", "set", "visible", "disabled", "text")
    /** Methods that return the same builder or row. */
    private val CHAIN_METHODS = KEY_METHODS + setOf("placeholders", "globalPlaceholders", "title")

    /** The key argument [literal] is, or null when it isn't one of a traceable Hytale menu call. */
    fun keyArgument(literal: PsiLiteralExpression): KeyArgument? {
        if (literal.value !is String) return null
        val call = PsiTreeUtil.getParentOfType(literal, PsiMethodCallExpression::class.java) ?: return null
        val args = call.argumentList.expressions
        val index = args.indexOfFirst { PsiUtil.skipParenthesizedExprDown(it) === literal }
        val method = call.methodExpression.referenceName ?: return null
        val valid = when (method) {
            in KEY_METHODS -> index == 0
            "addRow" -> index == 0 || index == 1
            else -> false
        }
        if (!valid || !isHytaleMenuMethod(call)) return null
        val target = target(call.methodExpression.qualifierExpression, 0) ?: return null
        return KeyArgument(call, method, index, target)
    }

    private fun isHytaleMenuMethod(call: PsiMethodCallExpression): Boolean {
        val owner = call.resolveMethod()?.containingClass?.qualifiedName ?: return false
        return owner == KCConstants.MENU_SCOPE || (owner == KCConstants.MENU_BUILDER && PlatformDetector.isHytale(call)) || owner == "com.golfing8.kcommon.menu.MenuRow"
    }

    fun target(expression: PsiExpression?, depth: Int): Target? {
        if (depth > 10) return null
        return when (val e = PsiUtil.skipParenthesizedExprDown(expression)) {
            is PsiMethodCallExpression -> {
                val args = e.argumentList.expressions
                when (e.methodExpression.referenceName) {
                    "getOrLoad" -> {
                        val loaded = (args.getOrNull(1) as? com.intellij.psi.PsiClassObjectAccessExpression)?.operand?.type?.let(PsiTypesUtil::getPsiClass)
                        if (loaded?.qualifiedName != KCConstants.MENU_BUILDER) return null
                        val path = constant(args[0]) ?: return null
                        Target(path.split('.').map(String::trim).filter(String::isNotEmpty), MenuContainerRegistry.bucketOf(e.project, e.methodExpression.qualifierExpression))
                    }
                    "orElseThrow", "get", "orElse" -> target(e.methodExpression.qualifierExpression, depth + 1)
                    "addRow" -> target(e.methodExpression.qualifierExpression, depth + 1)?.let { menu ->
                        args.getOrNull(1)?.let(::constant)?.let { menu.copy(template = it) }
                    }
                    in CHAIN_METHODS -> target(e.methodExpression.qualifierExpression, depth + 1)
                    else -> null
                }
            }
            is com.intellij.psi.PsiReferenceExpression -> when (val variable = e.resolve()) {
                is PsiLocalVariable -> target(variable.initializer, depth + 1)
                is PsiField -> target(variable.initializer, depth + 1)
                else -> null
            }
            else -> null
        }
    }

    private fun constant(expression: PsiExpression): String? =
        JavaPsiFacade.getInstance(expression.project).constantEvaluationHelper.computeConstantExpression(expression) as? String

    /** The YAML mappings of the menu [target] names, across the project's YAML files. */
    fun menus(project: Project, target: Target): List<YAMLMapping> {
        val byTarget = CachedValuesManager.getManager(project).getCachedValue(project) {
            CachedValueProvider.Result.create(java.util.concurrent.ConcurrentHashMap<Pair<List<String>, String?>, List<YAMLMapping>>(), PsiModificationTracker.MODIFICATION_COUNT)
        }
        return byTarget.getOrPut(target.path to target.bucket) {
            val scope = GlobalSearchScope.projectScope(project)
            val files = if (target.bucket != null) {
                listOf("yml", "yaml").flatMap { FilenameIndex.getVirtualFilesByName("${target.bucket}.$it", scope) }
            } else {
                listOf("yml", "yaml").flatMap { FilenameIndex.getAllFilesByExt(project, it, scope) }
            }
            val psi = PsiManager.getInstance(project)
            files.mapNotNull { file ->
                val yaml = psi.findFile(file) as? YAMLFile ?: return@mapNotNull null
                YAMLUtil.getQualifiedKeyInFile(yaml, target.path)?.value as? YAMLMapping
            }
        }
    }

    /** Element key -> its YAML key-value: the menu's element tree, or for a row, its template's tree (the template's own key included). */
    fun elements(menu: YAMLMapping, template: String?): Map<String, YAMLKeyValue> {
        val result = LinkedHashMap<String, YAMLKeyValue>()
        fun walk(mapping: YAMLMapping?) {
            for (kv in mapping?.keyValues.orEmpty()) {
                result.putIfAbsent(kv.keyText, kv)
                walk((kv.value as? YAMLMapping)?.getKeyValueByKey("children")?.value as? YAMLMapping)
            }
        }
        if (template == null) {
            menu.getKeyValueByKey("content")?.let { result["content"] = it }
            walk(menu.getKeyValueByKey("elements")?.value as? YAMLMapping)
        } else {
            val root = (menu.getKeyValueByKey("templates")?.value as? YAMLMapping)?.getKeyValueByKey(template) ?: return result
            result[template] = root
            walk((root.value as? YAMLMapping)?.getKeyValueByKey("children")?.value as? YAMLMapping)
        }
        return result
    }

    fun templates(menu: YAMLMapping): Map<String, YAMLKeyValue> =
        (menu.getKeyValueByKey("templates")?.value as? YAMLMapping)?.keyValues.orEmpty().associateBy { it.keyText }

    fun elementType(element: YAMLKeyValue): String =
        ((element.value as? YAMLMapping)?.getKeyValueByKey("type")?.value as? YAMLScalar)?.textValue ?: "Group"

    /** What a key argument may name: element keys, or for `addRow`, container elements or template keys. */
    fun candidates(argument: KeyArgument, menu: YAMLMapping): Map<String, YAMLKeyValue> = when {
        argument.method == "addRow" && argument.index == 1 -> templates(menu)
        argument.method == "addRow" -> elements(menu, argument.target.template)
        else -> elements(menu, argument.target.template)
    }

    /** Keys passed to `bindTo`/`show` anywhere in the project, or null when the project has no such calls (so nothing can be judged unbound). */
    fun boundKeys(project: Project): Set<String>? = CachedValuesManager.getManager(project).getCachedValue(project) {
        val scope = GlobalSearchScope.allScope(project)
        val scopeClass = JavaPsiFacade.getInstance(project).findClass(KCConstants.MENU_SCOPE, scope)
        val keys = HashSet<String>()
        var calls = 0
        for (method in scopeClass?.methods.orEmpty().filter { it.name == "bindTo" || it.name == "show" }) {
            for (reference in MethodReferencesSearch.search(method, scope, true)) {
                val call = PsiTreeUtil.getParentOfType(reference.element, PsiMethodCallExpression::class.java) ?: continue
                calls++
                call.argumentList.expressions.firstOrNull()?.let(::constant)?.let(keys::add)
            }
        }
        CachedValueProvider.Result.create(if (calls == 0) null else keys, PsiModificationTracker.MODIFICATION_COUNT)
    }
}

/** Ctrl+click and completion from a Java menu key string to its YAML element. */
class MenuKeyReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(PsiJavaPatterns.psiLiteral(), object : PsiReferenceProvider() {
            override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                val literal = element as? PsiLiteralExpression ?: return PsiReference.EMPTY_ARRAY
                val argument = MenuJavaSupport.keyArgument(literal) ?: return PsiReference.EMPTY_ARRAY
                return arrayOf(MenuKeyReference(literal, argument))
            }
        })
    }

    private class MenuKeyReference(literal: PsiLiteralExpression, private val argument: MenuJavaSupport.KeyArgument) :
        PsiPolyVariantReferenceBase<PsiLiteralExpression>(literal, TextRange(1, literal.textLength - 1), true) {

        override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
            val key = element.value as? String ?: return ResolveResult.EMPTY_ARRAY
            return MenuJavaSupport.menus(element.project, argument.target)
                .mapNotNull { MenuJavaSupport.candidates(argument, it)[key] }
                .map(::PsiElementResolveResult).toTypedArray()
        }

        override fun getVariants(): Array<Any> =
            MenuJavaSupport.menus(element.project, argument.target)
                .flatMap { MenuJavaSupport.candidates(argument, it).values }
                .distinctBy { it.keyText }
                .map { LookupElementBuilder.create(it.keyText).withTypeText(MenuJavaSupport.elementType(it), true) }
                .toTypedArray()
    }
}

/**
 * Flags Java menu calls that would throw when the menu is built: a key the menu doesn't have,
 * `text()` on an element without text, `set()` of a property the element type lacks, and `addRow`
 * into an element that can't hold rows or from a missing template.
 */
class HytaleMenuBindingInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = object : JavaElementVisitor() {
        override fun visitLiteralExpression(literal: PsiLiteralExpression) {
            val key = literal.value as? String ?: return
            val argument = MenuJavaSupport.keyArgument(literal) ?: return
            val menus = MenuJavaSupport.menus(literal.project, argument.target)
            if (menus.isEmpty()) return
            val where = argument.target.template?.let { "template '$it'" } ?: "menu '${argument.target.path.joinToString(".")}'"
            val matches = menus.mapNotNull { MenuJavaSupport.candidates(argument, it)[key] }
            if (matches.isEmpty()) {
                val what = if (argument.method == "addRow" && argument.index == 1) "template" else "element"
                holder.registerProblem(literal, "No $what '$key' in $where")
                return
            }
            val type = MenuJavaSupport.elementType(matches.first())
            val schema = HytaleMenuShapes.schema(literal.project)
            when {
                argument.method == "text" && type !in schema.textTypes ->
                    holder.registerProblem(literal, "'$key' is a $type, which has no text")
                argument.method == "addRow" && argument.index == 0 && type !in schema.containers ->
                    holder.registerProblem(literal, "'$key' is a $type, which can't hold rows")
                argument.method == "set" -> {
                    val property = argument.call.argumentList.expressions.getOrNull(1) ?: return
                    val name = JavaPsiFacade.getInstance(literal.project).constantEvaluationHelper.computeConstantExpression(property) as? String ?: return
                    val known = schema.propertyKeys(type) ?: return
                    val kebab = ConfigPsiUtil.camelToKebab(com.golfing8.kcommon.idea.preview.HytaleMenuHtml.pascal(name))
                    if (kebab !in known) holder.registerProblem(property, "A $type has no property '$name'")
                }
            }
        }
    }
}
