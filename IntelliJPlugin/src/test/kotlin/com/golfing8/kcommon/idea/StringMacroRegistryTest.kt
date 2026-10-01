package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Pins [StringMacroRegistry]'s hand-authored built-in table against
 * `com.golfing8.kcommon.util.string.StringMacros.DEFAULT`'s actual static initializer (KCommon
 * module, StringMacros.java) - if that class ever gains/loses/renames a macro, this should be
 * updated to match. Only [StringMacroRegistry.builtInMacros] is exercised here since
 * [StringMacroRegistry.allMacros] additionally does a project-wide PSI search for custom macros,
 * which needs the IntelliJ platform test fixtures this project doesn't have set up.
 */
class StringMacroRegistryTest {

    @Test
    fun `every built-in macro from StringMacros DEFAULT is present exactly once`() {
        val expected = setOf(
            "lc", "uc", "cap", "commas", "stripcommas", "rs", "sc",
            "roman", "replace", "repeat", "eval", "reverse", "substring", "int"
        )
        val symbols = StringMacroRegistry.builtInMacros().map { it.symbol }
        assertEquals(expected, symbols.toSet())
        assertEquals(expected.size, symbols.size, "a symbol is listed more than once")
    }

    @Test
    fun `zero-arg macros are modeled with no argument names`() {
        val zeroArg = setOf("lc", "uc", "cap", "commas", "stripcommas", "sc", "roman", "eval", "reverse", "int")
        for (macro in StringMacroRegistry.builtInMacros().filter { it.symbol in zeroArg }) {
            assertTrue(macro.argNames.isEmpty(), "${macro.symbol} should have no arg names")
        }
    }

    @Test
    fun `macros with required or optional parenthesized arguments are modeled with the right count`() {
        val byName = StringMacroRegistry.builtInMacros().associateBy { it.symbol }
        assertEquals(1, byName.getValue("rs").argNames.size)
        assertEquals(1, byName.getValue("repeat").argNames.size)
        assertEquals(2, byName.getValue("replace").argNames.size)
        assertEquals(2, byName.getValue("substring").argNames.size)
    }

    @Test
    fun `every built-in macro has a non-blank description`() {
        for (macro in StringMacroRegistry.builtInMacros()) {
            assertTrue(macro.description.isNotBlank(), "${macro.symbol} has a blank description")
        }
    }
}
