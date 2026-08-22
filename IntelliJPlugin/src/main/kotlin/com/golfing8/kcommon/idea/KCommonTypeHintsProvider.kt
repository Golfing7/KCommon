package com.golfing8.kcommon.idea

import com.intellij.codeInsight.hints.declarative.HintColorKind
import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.InlayHintsCollector
import com.intellij.codeInsight.hints.declarative.InlayHintsProvider
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.codeInsight.hints.declarative.SharedBypassCollector
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.yaml.psi.YAMLKeyValue

/**
 * Shows the resolved KCommon type after any config key whose type this plugin could positively
 * identify (enum, object, list, map) - toggleable per the standard Inlay Hints settings (Settings
 * | Editor | Inlay Hints | YAML | KCommon config types), and per-hint via right-click.
 */
class KCommonTypeHintsProvider : InlayHintsProvider {
    override fun createCollector(file: PsiFile, editor: Editor): InlayHintsCollector = Collector

    private object Collector : SharedBypassCollector {
        override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
            if (element !is YAMLKeyValue) return

            val schema = ConfigSchemaResolver.resolveForFile(element.containingFile) ?: return
            val path = ConfigSchemaResolver.buildKeyPath(element) ?: return
            val resolution = schema.resolve(path)
            val type = (resolution as? SchemaResolution.Resolved)?.type ?: return
            val label = describeType(type) ?: return

            // Always anchor at the very end of the key's own physical line - after its scalar
            // value and any trailing comment, if either is present, never right after the colon
            // and never at the start of a following (possibly nested/indented) line. A key is
            // always the first token on its line, so scanning the file text forward from the
            // key-value's start to the next newline lands exactly there, whether the value is an
            // inline scalar, a nested mapping/sequence starting below, or absent entirely.
            val fileText = element.containingFile.text
            val lineBreak = fileText.indexOf('\n', element.textRange.startOffset)
            val offset = if (lineBreak >= 0) lineBreak else fileText.length

            sink.addPresentation(
                InlineInlayPosition(offset, true),
                emptyList(),
                null,
                HintFormat.default.withColorKind(HintColorKind.TextWithoutBackground)
            ) {
                text(label)
            }
        }

        private fun describeType(type: ConfigFieldType): String? = when (type) {
            is ConfigFieldType.EnumLike -> "enum"
            is ConfigFieldType.Nested -> type.typeName
            is ConfigFieldType.ListOf -> describeType(type.inner)?.let { "list<$it>" } ?: "list"
            is ConfigFieldType.MapOf -> describeType(type.inner)?.let { "map<$it>" } ?: "map"
            ConfigFieldType.Unknown -> null
        }
    }
}
