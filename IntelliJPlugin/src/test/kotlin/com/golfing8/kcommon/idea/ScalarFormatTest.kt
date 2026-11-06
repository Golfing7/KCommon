package com.golfing8.kcommon.idea

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ScalarFormatTest {
    @Test
    fun `formats match what MenuMarkup accepts`() {
        assertNull(ScalarFormat.INTEGER.problem("-12"))
        assertEquals("must be a whole number", ScalarFormat.INTEGER.problem("1.5"))
        assertEquals("can't be a reference to a .ui value", ScalarFormat.INTEGER.problem("@Height"))
        assertNull(ScalarFormat.DECIMAL.problem("0.5"))
        assertNull(ScalarFormat.COLOR.problem("#000000(0.45)"))
        assertNotNull(ScalarFormat.COLOR.problem("#fff"))
        assertNull(ScalarFormat.PATCH.problem("Common/ContainerPatch.png"))
        assertNull(ScalarFormat.PATCH.problem("#ffffff"))
        assertNull(ScalarFormat.PATCH.problem("@PanelBackground"))
        assertNotNull(ScalarFormat.PATCH.problem("blue"))
        assertNull(ScalarFormat.REF.problem("Pages/BarterPage.ui@Row"))
        assertNotNull(ScalarFormat.REF.problem("DefaultTextButtonStyle"))
        // Placeholders are only checked once filled in.
        assertNull(ScalarFormat.INTEGER.problem("{WIDTH}"))
    }
}
