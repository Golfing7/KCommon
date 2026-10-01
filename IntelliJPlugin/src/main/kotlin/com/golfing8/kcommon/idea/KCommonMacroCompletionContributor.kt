package com.golfing8.kcommon.idea

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.codeInsight.template.TemplateManager
import com.intellij.codeInsight.template.impl.ConstantNode
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * Completion for KCommon's `$symbol(arg1)(arg2)...{content}` string macros - see
 * [StringMacroRegistry]/[com.golfing8.kcommon.util.string.StringMacros]. Fires when typing `$`
 * (optionally followed by a partial macro name) inside a YAML string *value* specifically - not a
 * key (checked explicitly below), and not a comment, so it never collides with the `#$Type` tag's
 * own `$`, which [KCommonTagCompletionContributor] handles (comments aren't a [YAMLScalar] at all).
 *
 * Accepting a suggestion inserts the symbol plus a real tab-stop template for its arguments and
 * content body (e.g. `replace` -> `replace(|)() {|}` with `|` marking stops), the same way the IDE's
 * own live templates behave, rather than just dropping in bare text the user has to hand-edit.
 */
class KCommonMacroCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val scalar = PsiTreeUtil.getParentOfType(parameters.position, YAMLScalar::class.java) ?: return
                    val enclosingKeyValue = PsiTreeUtil.getParentOfType(scalar, YAMLKeyValue::class.java)
                    if (enclosingKeyValue != null && enclosingKeyValue.key === scalar) return

                    val file = parameters.originalFile
                    val text = file.text
                    val offset = parameters.offset.coerceIn(0, text.length)
                    val scalarStart = scalar.textRange.startOffset
                    val windowStart = maxOf(scalarStart, offset - MAX_SYMBOL_SCAN_LENGTH).coerceAtMost(offset)
                    val typed = matchMacroPrefix(text.substring(windowStart, offset)) ?: return

                    val scoped = if (typed.isNotEmpty()) result.withPrefixMatcher(typed) else result
                    for (macro in StringMacroRegistry.allMacros(file.project)) {
                        scoped.addElement(buildLookupElement(macro))
                    }
                }
            }
        )
    }

    private fun buildLookupElement(macro: MacroDefinition): LookupElement {
        val tail = buildString {
            for (arg in macro.argNames) append('(').append(arg).append(')')
            append("{}")
        }
        return LookupElementBuilder.create(macro.symbol)
            .withTailText(tail, true)
            .withTypeText(macro.description, true)
            .withInsertHandler(MacroInsertHandler(macro))
    }

    private class MacroInsertHandler(private val macro: MacroDefinition) : InsertHandler<LookupElement> {
        override fun handleInsert(context: InsertionContext, item: LookupElement) {
            val editor = context.editor
            editor.caretModel.moveToOffset(context.tailOffset)

            val templateManager = TemplateManager.getInstance(context.project)
            val template = templateManager.createTemplate("", "")
            template.isToReformat = false
            for (argName in macro.argNames) {
                template.addTextSegment("(")
                template.addVariable(argName, ConstantNode(""), ConstantNode(""), true)
                template.addTextSegment(")")
            }
            template.addTextSegment("{")
            template.addVariable("content", ConstantNode(""), ConstantNode(""), true)
            template.addTextSegment("}")

            templateManager.startTemplate(editor, template)
        }
    }

    companion object {
        /** Macro symbol names are short - no need to scan further back than this looking for the triggering `$`. */
        internal const val MAX_SYMBOL_SCAN_LENGTH = 64
        private val MACRO_PREFIX = Regex("\\$([A-Za-z0-9_]*)$")

        /**
         * If [window] (the text of a YAML string value, from wherever scanning started up to the
         * caret) ends in `$` optionally followed by a partial macro name, returns that partial name
         * (empty string for a bare `$`) - otherwise null, meaning "don't offer macro completion here".
         * Pulled out as pure text-matching so it's directly testable without a PSI file.
         */
        internal fun matchMacroPrefix(window: String): String? = MACRO_PREFIX.find(window)?.groupValues?.get(1)
    }
}
