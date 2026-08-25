package com.golfing8.kcommon.idea

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext

/**
 * Smart completion for the plugin's own comment-based tags (see [FileTagParser]):
 *  - `#@Module <id>` - suggests every known KCommon module id (from this project's PSI plus
 *    anything imported into [ExternalSchemaRegistry]).
 *  - `#@File <bucket>` - suggests known config buckets, scoped to whichever module a `#@Module` tag
 *    in the same comment block (if any) names.
 *  - `#$Type <name>` - suggests every known named type (see [NamedTypeRegistry]).
 *
 * Driven by the raw text of the current line rather than YAML's comment PSI, since as far as the
 * YAML grammar is concerned these tags are just plain comments - there's no dedicated PSI to
 * pattern-match against, so this reads the line up to the caret directly instead.
 */
class KCommonTagCompletionContributor : CompletionContributor() {

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
                    val file = parameters.originalFile
                    val text = file.text
                    val offset = parameters.offset.coerceIn(0, text.length)
                    val lineStart = text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0)) + 1
                    val prefixLine = text.substring(lineStart, offset)
                    val trimmed = prefixLine.trimStart()
                    if (!trimmed.startsWith("#")) return
                    val afterHash = trimmed.trimStart('#').trimStart().trimEnd('\r')
                    val project = file.project

                    MODULE_TAG_PREFIX.find(afterHash)?.let { match ->
                        val ids = LinkedHashSet<String>()
                        ids += ConfigPsiUtil.allModulesByName(project).keys
                        ids += ExternalSchemaRegistry.getInstance(project).allModuleIds()
                        offerResults(result, match.groupValues[1], ids)
                        return
                    }

                    FILE_TAG_PREFIX.find(afterHash)?.let { match ->
                        val moduleId = nearbyModuleTag(text, lineStart)
                        val buckets = LinkedHashSet<String>()
                        if (moduleId != null) {
                            buckets += bucketsForModule(project, moduleId)
                        } else {
                            buckets += KCConstants.MAIN_CONFIG_BUCKET
                        }
                        offerResults(result, match.groupValues[1], buckets)
                        return
                    }

                    TYPE_TAG_PREFIX.find(afterHash)?.let { match ->
                        offerResults(result, match.groupValues[1], NamedTypeRegistry.allNames(project))
                        return
                    }
                }
            }
        )
    }

    private fun bucketsForModule(project: Project, moduleId: String): Set<String> {
        val buckets = LinkedHashSet<String>()
        ConfigPsiUtil.findModuleClassByName(project, moduleId)?.let { moduleClass ->
            val configSources = moduleClass.getAnnotation(KCConstants.MODULE_INFO)
                ?.let { ConfigPsiUtil.extractConfigSources(it) }
                .orEmpty()
            buckets += ConfigPsiUtil.collectBuckets(moduleClass, configSources)
        }
        buckets += ExternalSchemaRegistry.getInstance(project).allBuckets(moduleId)
        if (buckets.isEmpty()) buckets += KCConstants.MAIN_CONFIG_BUCKET
        return buckets
    }

    private fun offerResults(result: CompletionResultSet, typedPrefix: String, values: Collection<String>) {
        val scoped = if (typedPrefix.isNotEmpty()) result.withPrefixMatcher(typedPrefix) else result
        for (value in values.distinct()) {
            scoped.addElement(LookupElementBuilder.create(value))
        }
    }

    /** Looks for a `#@Module <id>` tag among the contiguous comment lines directly touching [lineStart] (scanning both upward and downward), to scope `#@File` completion to the right module's buckets. */
    private fun nearbyModuleTag(text: String, lineStart: Int): String? {
        val lines = text.lines()
        val lineIndex = text.substring(0, lineStart).count { it == '\n' }

        var i = lineIndex
        while (i >= 0 && i < lines.size && lines[i].trimStart().startsWith("#")) {
            MODULE_TAG_LINE.find(lines[i])?.let { return it.groupValues[1] }
            i--
        }
        i = lineIndex + 1
        while (i < lines.size && lines[i].trimStart().startsWith("#")) {
            MODULE_TAG_LINE.find(lines[i])?.let { return it.groupValues[1] }
            i++
        }
        return null
    }

    companion object {
        private val MODULE_TAG_PREFIX = Regex("^@Module\\s+(\\S*)$", RegexOption.IGNORE_CASE)
        private val FILE_TAG_PREFIX = Regex("^@File\\s+(\\S*)$", RegexOption.IGNORE_CASE)
        private val TYPE_TAG_PREFIX = Regex("^\\\$Type\\s+(\\S*)$", RegexOption.IGNORE_CASE)
        private val MODULE_TAG_LINE = Regex("@Module\\s+(\\S+)", RegexOption.IGNORE_CASE)
    }
}
