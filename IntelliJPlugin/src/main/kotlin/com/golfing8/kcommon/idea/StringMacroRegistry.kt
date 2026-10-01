package com.golfing8.kcommon.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil

/**
 * A single `$symbol(arg1)(arg2)...{content}` string macro (see
 * [com.golfing8.kcommon.util.string.StringMacros]'s own class doc for the exact grammar).
 * [argNames] names each optional parenthesized argument, in declaration order, purely for the
 * completion popup's tail text and the tab-stop template inserted on acceptance - it isn't used to
 * validate anything.
 */
data class MacroDefinition(val symbol: String, val argNames: List<String>, val description: String)

/**
 * Mirrors KCommon's built-in `StringMacros.DEFAULT` macros - a hand-authored table, the same way
 * [BuiltInAdapters] mirrors ConfigTypeRegistry's own static init block, since KCommon's macro
 * registration is a plain imperative `Map` populated in a static initializer, not
 * annotation/reflection-discoverable the way `@ConfigAdapterInfo`/`@ModuleInfo` are.
 *
 * A consuming project can still register its own macros at runtime via
 * `StringMacros.DEFAULT.registerMacro("name", ...)` though, so this also does a best-effort PSI
 * search for such calls (by symbol name only - a custom macro's argument shape/behavior lives in an
 * arbitrary lambda, nothing to introspect there) and merges them in, the same leniency-first spirit
 * as the rest of this plugin's project-extensibility discovery.
 */
object StringMacroRegistry {

    private val CUSTOM_MACROS_KEY: Key<CachedValue<List<MacroDefinition>>> = Key.create("kcommon.customMacros")

    private val builtIns: List<MacroDefinition> = listOf(
        MacroDefinition("lc", emptyList(), "Lowercases the content"),
        MacroDefinition("uc", emptyList(), "Uppercases the content"),
        MacroDefinition("cap", emptyList(), "Capitalizes the first letter of the content"),
        MacroDefinition("commas", emptyList(), "Formats the content (a number) with thousands separators"),
        MacroDefinition("stripcommas", emptyList(), "Removes commas from the content"),
        MacroDefinition("rs", listOf("length"), "A random string of `length` characters (default 8) drawn from the content's own characters"),
        MacroDefinition("sc", emptyList(), "Strips color codes from the content"),
        MacroDefinition("roman", emptyList(), "Converts the content (a number) to a roman numeral"),
        MacroDefinition("replace", listOf("from", "to"), "Replaces every occurrence of `from` with `to` in the content"),
        MacroDefinition("repeat", listOf("count"), "Repeats the content `count` times (default 2)"),
        MacroDefinition("eval", emptyList(), "Evaluates the content as a math expression"),
        MacroDefinition("reverse", emptyList(), "Reverses the content"),
        MacroDefinition("substring", listOf("begin", "end"), "The content's substring from `begin` to `end`"),
        MacroDefinition("int", emptyList(), "Formats the content (a number) as an integer"),
    )

    /** KCommon's hand-authored built-in macros, with no PSI/project involved - see the class doc for why these can't be discovered instead. */
    fun builtInMacros(): List<MacroDefinition> = builtIns

    /** Every macro available for completion: KCommon's built-ins plus this project's own custom-registered ones (built-ins win a symbol collision). */
    fun allMacros(project: Project): List<MacroDefinition> {
        val builtInSymbols = builtIns.mapTo(HashSet()) { it.symbol }
        return builtIns + customMacros(project).filter { it.symbol !in builtInSymbols }
    }

    private fun customMacros(project: Project): List<MacroDefinition> {
        return CachedValuesManager.getManager(project).getCachedValue(project, CUSTOM_MACROS_KEY, {
            val macroClass = JavaPsiFacade.getInstance(project)
                .findClass(KCConstants.STRING_MACROS, GlobalSearchScope.allScope(project))
            val registerMethod = macroClass?.findMethodsByName("registerMacro", false)?.firstOrNull()

            val result: List<MacroDefinition> = if (registerMethod == null) {
                emptyList()
            } else {
                val scope = GlobalSearchScope.allScope(project)
                val names = LinkedHashSet<String>()
                for (reference in MethodReferencesSearch.search(registerMethod, scope, true)) {
                    val call = PsiTreeUtil.getParentOfType(reference.element, PsiMethodCallExpression::class.java) ?: continue
                    val firstArg = call.argumentList.expressions.getOrNull(0) ?: continue
                    constantString(project, firstArg)?.let { names += it }
                }
                names.map { MacroDefinition(it, emptyList(), "Custom macro") }
            }

            CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT)
        }, false)
    }

    private fun constantString(project: Project, expression: PsiExpression): String? =
        JavaPsiFacade.getInstance(project).constantEvaluationHelper.computeConstantExpression(expression) as? String
}
