package com.golfing8.kcommon.idea

import com.golfing8.kcommon.idea.preview.HytaleUiAssets
import com.golfing8.kcommon.idea.preview.MenuAssets
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import org.jetbrains.yaml.psi.YAMLDocument
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar

/**
 * Flags YAML keys/values in a `<module-id>/config.yml` that don't match the KCommon module's
 * declared Java config structure. Everything here is a weak warning at most, never an error -
 * per spec, an unresolved module or field type means we simply say nothing.
 *
 * For Hytale menus it also mirrors the checks `MenuBuilder` makes when it loads (value formats,
 * duplicate element keys, `.ui` references that point nowhere), and flags `special` elements no
 * Java code ever binds or shows. In a lang.yml it lists the messages the file doesn't define yet.
 */
class KCommonYamlAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is YAMLKeyValue) return

        val (moduleId, resolution) = ConfigSchemaResolver.resolveKey(element)

        when (resolution) {
            is SchemaResolution.UnknownKey -> {
                if (resolution.parentConfident && moduleId != null) {
                    val anchor = element.key?.textRange ?: element.textRange
                    holder.newAnnotation(
                        HighlightSeverity.WEAK_WARNING,
                        "Unknown config key '${element.keyText}' for module '$moduleId'"
                    ).range(anchor).create()
                }
            }
            is SchemaResolution.Resolved -> {
                checkEnumValue(element, resolution.type, holder)
                checkScalar(element, resolution.type, holder)
                val type = resolution.type as? ConfigFieldType.Nested
                if (type?.typeName == HytaleMenuShapes.MENU_TYPE_NAME) checkDuplicateKeys(element, holder)
                if (element.keyText == "special") checkSpecialBound(element, holder)
            }
            SchemaResolution.Indeterminate -> Unit
        }
        checkMissingLangKeys(element, holder)
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
        // A placeholder is only filled in at runtime.
        if (text.contains('{')) return

        val validNames = EnumSource.resolve(enumType.source, element.project)
        if (validNames.isEmpty()) return
        if (validNames.any { it.equals(text, ignoreCase = true) }) return

        val preview = validNames.take(8).joinToString(", ") + if (validNames.size > 8) ", ..." else ""
        holder.newAnnotation(
            HighlightSeverity.WEAK_WARNING,
            "'$text' is not a recognized value. Expected one of: $preview"
        ).range(scalar.textRange).create()
    }

    /** A Hytale menu value in the wrong format, or a `.ui` reference that points nowhere. */
    private fun checkScalar(element: YAMLKeyValue, type: ConfigFieldType, holder: AnnotationHolder) {
        val scalar = element.value as? YAMLScalar ?: return
        val format = when (type) {
            is ConfigFieldType.Formatted -> type
            is ConfigFieldType.Nested -> type.scalar
            else -> null
        } ?: return
        val text = scalar.textValue.trim()
        val problem = format.format.problem(text)
        if (problem != null) {
            holder.newAnnotation(HighlightSeverity.WEAK_WARNING, "'${element.keyText}' $problem").range(scalar.textRange).create()
            return
        }
        if (!ScalarFormat.REFERENCE.matches(text)) return
        val assets = HytaleUiAssets.current() ?: return
        val message = when (assets.referenceStatus(text)) {
            MenuAssets.RefStatus.NO_DOCUMENT -> "There's no .ui document '${text.substringBeforeLast('@')}' in the Hytale assets"
            MenuAssets.RefStatus.NO_VALUE -> "'$text' isn't defined in ${text.substringBeforeLast('@').ifEmpty { "Common.ui" }}"
            MenuAssets.RefStatus.OK -> {
                val actual = assets.referenceNames(text.substringBeforeLast('@').ifEmpty { null })[text.substringAfterLast('@')]
                val expected = format.uiType
                if (actual != null && expected != null && actual != expected) "'$text' is a $actual, not a $expected" else null
            }
        } ?: return
        holder.newAnnotation(HighlightSeverity.WEAK_WARNING, message).range(scalar.textRange).create()
    }

    /** `MenuElement.parse` rejects a key used twice in the menu's element tree (where `content` is taken) or within one template. */
    private fun checkDuplicateKeys(menu: YAMLKeyValue, holder: AnnotationHolder) {
        val mapping = menu.value as? YAMLMapping ?: return
        fun walk(elements: YAMLMapping?, seen: MutableSet<String>) {
            for (kv in elements?.keyValues.orEmpty()) {
                if (!seen.add(kv.keyText)) {
                    holder.newAnnotation(HighlightSeverity.WARNING, "The key '${kv.keyText}' is used twice in this menu; element keys must be unique")
                        .range(kv.key?.textRange ?: kv.textRange).create()
                }
                walk((kv.value as? YAMLMapping)?.getKeyValueByKey("children")?.value as? YAMLMapping, seen)
            }
        }
        walk(mapping.getKeyValueByKey("elements")?.value as? YAMLMapping, mutableSetOf("content"))
        for (template in (mapping.getKeyValueByKey("templates")?.value as? YAMLMapping)?.keyValues.orEmpty()) {
            walk((template.value as? YAMLMapping)?.getKeyValueByKey("children")?.value as? YAMLMapping, mutableSetOf(template.keyText))
        }
    }

    /** A `special: true` element only appears once Java binds or shows it. */
    private fun checkSpecialBound(special: YAMLKeyValue, holder: AnnotationHolder) {
        if ((special.value as? YAMLScalar)?.textValue != "true") return
        val owner = (special.parent as? YAMLMapping)?.parent as? YAMLKeyValue ?: return
        val ownerType = (ConfigSchemaResolver.resolveKey(owner).resolution as? SchemaResolution.Resolved)?.type as? ConfigFieldType.Nested
        if (ownerType?.typeName != "MenuElement") return
        val bound = MenuJavaSupport.boundKeys(special.project) ?: return
        if (owner.keyText in bound) return
        holder.newAnnotation(
            HighlightSeverity.WEAK_WARNING,
            "'${owner.keyText}' is special but no bindTo or show call names it, so it never appears"
        ).range(special.textRange).create()
    }

    /** On the first key of a lang.yml, the messages the module defines that the file doesn't have yet. */
    private fun checkMissingLangKeys(element: YAMLKeyValue, holder: AnnotationHolder) {
        val root = element.parent as? YAMLMapping ?: return
        if (root.parent !is YAMLDocument || root.keyValues.firstOrNull() !== element) return
        val schema = ConfigSchemaResolver.resolveForFile(element.containingFile) ?: return
        if (!schema.bucket.equals(KCConstants.LANG_BUCKET, ignoreCase = true)) return
        val missing = ArrayList<String>()
        fun walk(fields: Map<String, ConfigFieldType>, mapping: YAMLMapping, prefix: String) {
            for ((key, type) in fields) {
                val present = mapping.keyValues.firstOrNull { it.keyText.equals(key, ignoreCase = true) }
                when {
                    present == null -> missing += prefix + key
                    // Only the synthetic groups made from dotted keys are walked into, not the messages themselves.
                    type is ConfigFieldType.Nested && type.typeName == "object" ->
                        (present.value as? YAMLMapping)?.let { walk(type.fields, it, "$prefix$key.") }
                }
            }
        }
        walk(schema.fieldsAt(emptyList())?.fields ?: return, root, "")
        if (missing.isEmpty()) return
        holder.newAnnotation(
            HighlightSeverity.WEAK_WARNING,
            "This lang file doesn't define ${missing.joinToString(", ")}; the defaults are written when the module loads"
        ).range(element.key?.textRange ?: element.textRange).create()
    }
}
