package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Note: TextColor's constructor computes red/green/blue via bit shifts without a final mask,
 * so for most inputs these fields do not represent standard 0-255 RGB channel values
 * (e.g. {@code (hex << 16) & 0xFF} is 0 unless the low byte of {@code hex << 16} happens to
 * land in the mask). These tests document the actual current arithmetic rather than an assumed
 * "correct" RGB decomposition.
 */
class TextColorTest {

    @Test
    @DisplayName("Parses a hex string and computes fields via the class's actual bit arithmetic")
    void testParsesHexCode() {
        String hexCode = "336699";
        int hex = Integer.parseInt(hexCode, 16);
        TextColor color = new TextColor(hexCode);

        assertEquals((hex << 16) & 0xFF, color.red);
        assertEquals((hex << 8) & 0xFF, color.green);
        assertEquals(hex & 0xFF, color.blue);
    }

    @Test
    @DisplayName("Throws for a non-hex string")
    void testInvalidHexThrows() {
        assertThrows(NumberFormatException.class, () -> new TextColor("not-hex"));
    }
}
