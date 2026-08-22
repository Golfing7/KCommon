package com.golfing8.kcommon.idea

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping

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
                    // completion clones for analysis - its VirtualFile has no parent, so schema
                    // resolution (which walks the real file's directory tree) must use the
                    // original, on-disk-backed file instead. Path-building is fine on the copy
                    // since it only needs PSI structure, not the VirtualFile.
                    val originalFile = parameters.originalFile
                    val schema = ConfigSchemaResolver.resolveForFile(originalFile) ?: return
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
                        val enclosingPath = ConfigSchemaResolver.buildKeyPath(enclosingKeyValue)
                        val enclosingType = enclosingPath?.let { (schema.resolve(it) as? SchemaResolution.Resolved)?.type }
                        if (enclosingType is ConfigFieldType.Nested) {
                            val existingValue = enclosingKeyValue.value
                            val existingKeys = if (existingValue is YAMLMapping) {
                                existingValue.keyValues.map { it.keyText.lowercase() }.toSet()
                            } else {
                                emptySet()
                            }
                            suggestFields(enclosingType.typeName, enclosingType.fields, existingKeys, result)
                            return
                        }

                        // Not object-shaped per our schema - only treat this as value-editing if
                        // the caret is actually inside a non-mapping value of this key.
                        val value = enclosingKeyValue.value
                        if (value != null && value !is YAMLMapping && PsiTreeUtil.isAncestor(value, position, false)) {
                            suggestValues(enclosingKeyValue, schema, originalFile.project, result)
                            return
                        }
                    }

                    val mapping = PsiTreeUtil.getParentOfType(position, YAMLMapping::class.java) ?: return
                    suggestKeysForMapping(mapping, schema, result)
                }
            }
        )
    }

    private fun suggestValues(
        keyValue: YAMLKeyValue,
        schema: ConfigSchema,
        project: Project,
        result: CompletionResultSet
    ) {
        val path = ConfigSchemaResolver.buildKeyPath(keyValue) ?: return
        val resolution = schema.resolve(path)
        val type = (resolution as? SchemaResolution.Resolved)?.type as? ConfigFieldType.EnumLike ?: return

        for (name in EnumSource.resolve(type.source, project)) {
            result.addElement(LookupElementBuilder.create(name))
        }
    }

    private fun suggestKeysForMapping(mapping: YAMLMapping, schema: ConfigSchema, result: CompletionResultSet) {
        val containerPath = ConfigSchemaResolver.buildContainerPath(mapping) ?: return
        val container = schema.fieldsAt(containerPath) ?: return
        val existingKeys = mapping.keyValues.map { it.keyText.lowercase() }.toSet()
        suggestFields(container.typeName, container.fields, existingKeys, result)
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
