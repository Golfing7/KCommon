package com.golfing8.kcommon.idea

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

/** A module/bucket override declared via a `#@Module`/`#@File` comment tag - see [FileTagParser]. */
data class ModuleFileTag(val moduleId: String, val bucket: String?)

/**
 * Parses the plugin's own comment-based tags, letting a YAML file (or a section/key within one)
 * declare its KCommon module and field types explicitly instead of relying purely on the
 * `<module-id>/config.yml` directory convention - a convention that doesn't hold for a file opened
 * standalone, e.g. downloaded via an SFTP/FileZilla-style remote-edit workflow outside its project.
 *
 * Recognized tags, each written as its own full-line YAML comment:
 *  - `#@Module <module-id>` - declares the KCommon module a file, or a specific section, belongs to.
 *  - `#@File <bucket>` - declares which of that module's config buckets (e.g. "config", "limits")
 *    a file/section corresponds to; defaults to the module's main "config" bucket when omitted.
 *  - `#$Type <TypeName>` - declares the exact resolved type of the key/value pair immediately
 *    following it, looked up by name via [NamedTypeRegistry].
 *
 * `#@Module`/`#@File` may appear at the top of a file, before any real content, to tag the whole
 * file (see [fileTag]), or directly above any YAML key to scope just that key's nested value to a
 * different module (see [sectionTag]) - [ConfigSchemaResolver] walks outward from a key looking for
 * the nearest one of either. Resolution here is deliberately lenient: a malformed or unmatched tag
 * simply yields null, same as no tag at all, never an error.
 */
object FileTagParser {

    private val MODULE_TAG = Regex("""^#+\s*@Module\s+(\S+)""", RegexOption.IGNORE_CASE)
    private val FILE_TAG = Regex("""^#+\s*@File\s+(\S+)""", RegexOption.IGNORE_CASE)
    private val TYPE_TAG = Regex("^#+\\s*\\\$Type\\s+(\\S+)", RegexOption.IGNORE_CASE)

    /** The `#@Module`/`#@File` tag declared directly above [element] (typically a YAMLKeyValue), if any. */
    fun sectionTag(element: PsiElement): ModuleFileTag? = parseModuleTag(precedingCommentLines(element))

    /** The `#@Module`/`#@File` tag declared at the very top of [file], before any real content. */
    fun fileTag(file: PsiFile): ModuleFileTag? = parseModuleTag(leadingFileCommentLines(file))

    /** The `#$Type` tag declared directly above [element] (typically a YAMLKeyValue), if any. */
    fun typeTag(element: PsiElement): String? {
        for (line in precedingCommentLines(element)) {
            TYPE_TAG.find(line)?.let { return it.groupValues[1] }
        }
        return null
    }

    private fun parseModuleTag(lines: List<String>): ModuleFileTag? {
        var moduleId: String? = null
        var bucket: String? = null
        for (line in lines) {
            MODULE_TAG.find(line)?.let { moduleId = it.groupValues[1] }
            FILE_TAG.find(line)?.let { bucket = it.groupValues[1] }
        }
        return moduleId?.let { ModuleFileTag(it, bucket) }
    }

    /**
     * Consecutive full-line comments immediately preceding [element] (in source order), found by
     * scanning [element]'s containing file's raw text backward line-by-line from [element]'s own
     * line - deliberately not a PSI tree walk (e.g. via `PsiTreeUtil.prevLeaf`): several JetBrains
     * language plugins attach a leading comment as a *child* of the following element rather than as
     * its preceding sibling, which would make a leaf-walk starting at that element skip right past
     * its own leading comment. Raw text has no such ambiguity - a full-line comment is a full-line
     * comment regardless of how the PSI groups it. Stops at the first blank or non-comment line -
     * a tag block must sit directly above what it tags, on its own lines.
     */
    private fun precedingCommentLines(element: PsiElement): List<String> {
        val file = element.containingFile ?: return emptyList()
        val elementStart = element.textRange?.startOffset ?: return emptyList()
        return commentLinesBefore(file.text, elementStart)
    }

    /**
     * The pure text-scanning core of [precedingCommentLines], pulled out so it's testable without a
     * PSI file: every contiguous full-line comment immediately above line-containing-[offset], in
     * source order, stopping at the first blank or non-comment line.
     */
    internal fun commentLinesBefore(text: String, offset: Int): List<String> {
        var cursor = text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0))
        if (cursor < 0 || cursor >= offset) return emptyList()

        val lines = ArrayDeque<String>()
        while (cursor >= 0) {
            val lineStart = text.lastIndexOf('\n', cursor - 1) + 1
            val line = text.substring(lineStart, cursor).trim()
            if (line.isEmpty() || !line.startsWith("#")) break
            lines.addFirst(line)
            cursor = lineStart - 1
        }
        return lines.toList()
    }

    /** Leading comment lines at the very start of the file's raw text, before the first non-blank, non-comment line. */
    private fun leadingFileCommentLines(file: PsiFile): List<String> {
        val lines = mutableListOf<String>()
        for (rawLine in file.text.lineSequence()) {
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) continue
            if (!trimmed.startsWith("#")) break
            lines.add(trimmed)
        }
        return lines
    }
}
