package com.golfing8.kcommon.idea

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * Flags YAML keys/values in a `<module-id>/config.yml` that don't match the KCommon module's
 * declared Java config structure. Everything here is a weak warning at most, never an error -
 * per spec, an unresolved module or field type means we simply say nothing.
 */
class KCommonYamlAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLKeyValue) return

        val file = element.containingFile ?: return
        val schema = ConfigSchemaResolver.resolveForFile(file) ?: return
        val path = ConfigSchemaResolver.buildKeyPath(element) ?: return

        when (val resolution = schema.resolve(path)) {
            is SchemaResolution.UnknownKey -> {
                if (resolution.parentConfident) {
                    val anchor = element.key?.textRange ?: element.textRange
                    holder.newAnnotation(
                        HighlightSeverity.WEAK_WARNING,
                        "Unknown config key '${element.keyText}' for module '${schema.moduleId}'"
                    ).range(anchor).create()
                }
            }
            is SchemaResolution.Resolved -> checkEnumValue(element, resolution.type, holder)
            SchemaResolution.Indeterminate -> Unit
        }
    }

    private fun checkEnumValue(
        element: YAMLKeyValue,
        type: ConfigFieldType,
        holder: AnnotationHolder
    ) {
        val enumType = type as? ConfigFieldType.EnumLike ?: return
        val scalar = element.value as? YAMLScalar ?: return
        val text = scalar.textValue
        if (text.isBlank()) return
        // Command items (ItemStackBuilder#type values like "/give ...") aren't real material
        // names, so there's nothing to validate against - just leave them unflagged.
        if (text.startsWith("/")) return

        val validNames = EnumSource.resolve(enumType.source, element.project)
        if (validNames.isEmpty()) return
        if (validNames.any { it.equals(text, ignoreCase = true) }) return

        val preview = validNames.take(8).joinToString(", ") + if (validNames.size > 8) ", ..." else ""
        holder.newAnnotation(
            HighlightSeverity.WEAK_WARNING,
            "'$text' is not a recognized value. Expected one of: $preview"
        ).range(scalar.textRange).create()
    }
}
