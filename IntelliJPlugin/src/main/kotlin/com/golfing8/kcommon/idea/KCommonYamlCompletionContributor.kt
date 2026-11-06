package com.golfing8.kcommon.idea

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.jetbrains.yaml.psi.YAMLScalar
import com.golfing8.kcommon.idea.preview.HytaleUiAssets

/**
 * Two kinds of suggestions, both only firing when the module and field type can be confidently
 * resolved - otherwise this contributes nothing and normal YAML completion takes over:
 *  - Typing a value for a field resolved to an enum-like type: suggest its valid values.
 *  - Typing a key (new or partial) inside a mapping resolved to a Nested type: suggest the
 *    fields of that type not already present as siblings, tagged with the type they come from.
 */
class KCommonYamlCompletionContributor : CompletionContributor() {

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
                    // parameters.position/keyValue live in a non-physical copy of the file that
                    // completion clones for analysis - its VirtualFile has no parent, so any
                    // directory-convention/file-tag resolution must fall back to the original,
                    // on-disk-backed file instead - see ConfigSchemaResolver.resolveKey's
                    // `fallbackFile` doc. Path/tag-walking is fine on the copy since it only needs
                    // PSI structure, not the VirtualFile.
                    val originalFile = parameters.originalFile
                    val position = parameters.position

                    // getParentOfType walks UP until it finds a YAMLKeyValue. While typing a
                    // brand-new key (no ':' yet), no YAMLKeyValue exists for that fragment, so
                    // this instead returns the *enclosing* key (e.g. "item:") - its .key is
                    // "item", not what's being typed.
                    val enclosingKeyValue = PsiTreeUtil.getParentOfType(position, YAMLKeyValue::class.java)

                    if (enclosingKeyValue != null) {
                        // A key with nothing (or only an in-progress colon-less fragment) after
                        // its ':' is genuinely ambiguous to YAML's parser: "foo:\n  ty" could be
                        // a scalar value "ty" continuing on the next line, or the start of a
                        // nested mapping whose first key is "ty" - the parser has to guess, and
                        // guesses scalar. Our schema knows better than that guess, so check it
                        // FIRST: if the enclosing key resolves to a Nested type, trust that and
                        // suggest its keys, regardless of how the value currently parses.
                        val enclosingResolution = ConfigSchemaResolver.resolveKey(enclosingKeyValue, originalFile).resolution
                        val enclosingType = (enclosingResolution as? SchemaResolution.Resolved)?.type
                        val scalarValue = enclosingKeyValue.value as? YAMLScalar
                        if (scalarValue != null && PsiTreeUtil.isAncestor(scalarValue, position, false) && suggestReferences(enclosingType, scalarValue, parameters, result)) {
                            return
                        }
                        if (enclosingType is ConfigFieldType.Nested) {
                            val existingValue = enclosingKeyValue.value as? YAMLMapping
                            val existingKeys = existingValue?.keyValues?.map { it.keyText.lowercase() }?.toSet().orEmpty()
                            val fields = ConfigSchemaResolver.fieldsOf(enclosingType, existingValue)
                            suggestFields(enclosingType.typeName, fields, existingKeys, result)
                            return
                        }
                        if (enclosingType is ConfigFieldType.MapOf) {
                            val keyType = enclosingType.keyType as? ConfigFieldType.EnumLike
                            if (keyType != null) {
                                suggestEnumKeys(keyType, originalFile.project, result)
                                return
                            }
                        }

                        // Not object-shaped per our schema - only treat this as value-editing if
                        // the caret is actually inside a non-mapping value of this key.
                        val value = enclosingKeyValue.value
                        if (value != null && value !is YAMLMapping && PsiTreeUtil.isAncestor(value, position, false)) {
                            suggestValues(enclosingKeyValue, originalFile, originalFile.project, result)
                            return
                        }
                    }

                    val mapping = PsiTreeUtil.getParentOfType(position, YAMLMapping::class.java) ?: return
                    suggestKeysForMapping(mapping, originalFile, originalFile.project, result)
                }
            }
        )
    }

    private fun suggestValues(
        keyValue: YAMLKeyValue,
        fallbackFile: PsiFile,
        project: Project,
        result: CompletionResultSet
    ) {
        var resolvedType = (ConfigSchemaResolver.resolveKey(keyValue, fallbackFile).resolution as? SchemaResolution.Resolved)?.type
        // A sequence item's own value is still resolved against the key's own type (e.g. list<enum>) -
        // unwrap to the element type so completions fire for `- <cursor>` entries too.
        while (resolvedType is ConfigFieldType.ListOf) {
            resolvedType = resolvedType.inner
        }
        val type = resolvedType as? ConfigFieldType.EnumLike ?: return

        for (name in EnumSource.resolve(type.source, project)) {
            result.addElement(LookupElementBuilder.create(name))
        }
    }

    /**
     * `.ui` references for a Hytale menu value that takes one: `@Name`s of Common.ui (only those of
     * the expected `.ui` type when it's known), the vanilla documents, and after `Doc.ui@` that
     * document's names. Returns false when the value doesn't take a reference here.
     */
    private fun suggestReferences(type: ConfigFieldType?, scalar: YAMLScalar, parameters: CompletionParameters, result: CompletionResultSet): Boolean {
        val format = when (type) {
            is ConfigFieldType.Formatted -> type
            is ConfigFieldType.Nested -> type.scalar
            else -> null
        } ?: return false
        if (format.format != ScalarFormat.REF && format.format != ScalarFormat.PATCH) return false
        val assets = HytaleUiAssets.current() ?: return false

        val text = parameters.position.containingFile.text
        val start = scalar.textRange.startOffset
        val quoted = text.getOrNull(start) == '\'' || text.getOrNull(start) == '"'
        val typed = text.substring((start + if (quoted) 1 else 0).coerceAtMost(parameters.offset), parameters.offset)
        if (format.format == ScalarFormat.PATCH && !typed.startsWith("@") && !typed.contains(".ui")) return false

        val scoped = result.withPrefixMatcher(typed)
        fun offer(value: String, typeText: String?) {
            scoped.addElement(LookupElementBuilder.create(value).withTypeText(typeText, true).withInsertHandler { context, _ ->
                // '@' can't start a plain YAML scalar, so an unquoted reference gets quotes.
                if (!quoted) context.document.replaceString(context.startOffset, context.tailOffset, "'$value'")
            })
        }
        val at = typed.indexOf(".ui@")
        if (at >= 0) {
            val document = typed.substring(0, at + 3)
            for ((name, uiType) in assets.referenceNames(document)) offer("$document@$name", uiType)
            return true
        }
        for ((name, uiType) in assets.referenceNames()) {
            if (format.uiType != null && uiType != null && uiType != format.uiType) continue
            if (assets.reference("@$name") == null) continue // element templates can't be referenced as values
            offer("@$name", uiType)
        }
        for (document in assets.documents()) offer("$document@", "document")
        return true
    }

    private fun suggestKeysForMapping(mapping: YAMLMapping, fallbackFile: PsiFile, project: Project, result: CompletionResultSet) {
        val resolution = ConfigSchemaResolver.resolveContainer(mapping, fallbackFile)
        val container = resolution.fields
        if (container != null) {
            val existingKeys = mapping.keyValues.map { it.keyText.lowercase() }.toSet()
            suggestFields(container.typeName, container.fields, existingKeys, result)
            return
        }

        val keyType = resolution.mapKeyType ?: return
        suggestEnumKeys(keyType, project, result)
    }

    private fun suggestEnumKeys(keyType: ConfigFieldType.EnumLike, project: Project, result: CompletionResultSet) {
        for (name in EnumSource.resolve(keyType.source, project)) {
            result.addElement(LookupElementBuilder.create(name))
        }
    }

    private fun suggestFields(
        typeName: String,
        fields: Map<String, ConfigFieldType>,
        existingKeys: Set<String>,
        result: CompletionResultSet
    ) {
        for (key in fields.keys) {
            if (key.lowercase() in existingKeys) continue

            result.addElement(LookupElementBuilder.create(key).withTypeText(typeName, true))
        }
    }
}
