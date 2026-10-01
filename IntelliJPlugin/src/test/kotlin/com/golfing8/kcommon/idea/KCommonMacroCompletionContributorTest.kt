package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/** Exercises [KCommonMacroCompletionContributor.matchMacroPrefix] directly - the pure text-matching core deciding whether/what to offer completion for, without needing a live PSI file. */
class KCommonMacroCompletionContributorTest {

    @Test
    fun `a bare dollar sign matches an empty prefix`() {
        assertEquals("", KCommonMacroCompletionContributor.matchMacroPrefix("some text \$"))
    }

    @Test
    fun `a dollar sign followed by a partial macro name matches that name`() {
        assertEquals("re", KCommonMacroCompletionContributor.matchMacroPrefix("prefix \$re"))
        assertEquals("stripcommas", KCommonMacroCompletionContributor.matchMacroPrefix("\$stripcommas"))
    }

    @Test
    fun `underscores and digits are valid partial-name characters`() {
        assertEquals("foo_123", KCommonMacroCompletionContributor.matchMacroPrefix("\$foo_123"))
    }

    @Test
    fun `no dollar sign at all yields no match`() {
        assertNull(KCommonMacroCompletionContributor.matchMacroPrefix("just some plain text"))
    }

    @Test
    fun `a dollar sign not at the end of the window (already moved past it) yields no match`() {
        assertNull(KCommonMacroCompletionContributor.matchMacroPrefix("\$lc{already closed} more text"))
    }

    @Test
    fun `a space breaks the partial macro name`() {
        assertNull(KCommonMacroCompletionContributor.matchMacroPrefix("\$re al"))
    }

    @Test
    fun `an empty window yields no match`() {
        assertNull(KCommonMacroCompletionContributor.matchMacroPrefix(""))
    }
}
