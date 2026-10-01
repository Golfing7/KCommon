package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Exercises [FileTagParser.commentLinesBefore] directly with plain strings - the pure core of
 * [FileTagParser.sectionTag]/[FileTagParser.typeTag], pulled out specifically so this offset math
 * can be verified without needing a live PSI file (which would need the IntelliJ platform's test
 * fixtures, not set up in this project).
 */
class FileTagParserTest {

    @Test
    fun `a single comment line directly above the element is found`() {
        val text = "#\$Type ArmorGroup\ncrate-preview-menu: MenuBuilder\n"
        val offset = text.indexOf("crate-preview-menu")
        assertEquals(listOf("#\$Type ArmorGroup"), FileTagParser.commentLinesBefore(text, offset))
    }

    @Test
    fun `stacked comment lines are returned in source order`() {
        val text = "#@Module crates\n#@File config\n#\$Type ArmorGroup\nkey: value\n"
        val offset = text.indexOf("key: value")
        assertEquals(
            listOf("#@Module crates", "#@File config", "#\$Type ArmorGroup"),
            FileTagParser.commentLinesBefore(text, offset)
        )
    }

    @Test
    fun `a blank line separating a comment block from the element breaks the scan there`() {
        // Mirrors the reported bug's file shape: tags separated by blank lines, then the #$Type tag
        // directly (no blank line) above the key it targets.
        val text = "#@Module crates\n\n#@File config\n\n\n#\$Type ArmorGroup\ncrate-preview-menu: MenuBuilder\n"
        val offset = text.indexOf("crate-preview-menu")
        assertEquals(listOf("#\$Type ArmorGroup"), FileTagParser.commentLinesBefore(text, offset))
    }

    @Test
    fun `a non-comment line directly above the element stops the scan without consuming it`() {
        val text = "some-key: 5\n#@Module arena\nnested-key: value\n"
        val offset = text.indexOf("nested-key")
        assertEquals(listOf("#@Module arena"), FileTagParser.commentLinesBefore(text, offset))
    }

    @Test
    fun `indented comments are still recognized`() {
        val text = "parent:\n  #\$Type Foo\n  child: value\n"
        val offset = text.indexOf("child: value")
        assertEquals(listOf("#\$Type Foo"), FileTagParser.commentLinesBefore(text, offset))
    }

    @Test
    fun `no preceding comment yields an empty list`() {
        val text = "key: value\n"
        assertTrue(FileTagParser.commentLinesBefore(text, text.indexOf("key")).isEmpty())
    }

    @Test
    fun `an element on the file's first line has nothing preceding it`() {
        val text = "key: value\n"
        assertTrue(FileTagParser.commentLinesBefore(text, 0).isEmpty())
    }
}
