package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressBarTest {

    @Test
    @DisplayName("Bar contains exactly `length` bar characters")
    void testBarLength() {
        String bar = ProgressBar.getProgressBar(50.0, 100.0, 20);
        assertEquals(20, bar.chars().filter(c -> c == ProgressBar.BOX_UNICODE).count());
    }

    @Test
    @DisplayName("Progress at or above the max fills every slot with the filled color")
    void testFullyFilled() {
        String bar = ProgressBar.getProgressBar(100.0, 100.0, 'X', 5, "F", "S", "E");
        assertEquals("FXFXFXFXFX", bar);
    }

    @Test
    @DisplayName("Zero progress produces no filled slots")
    void testZeroProgressHasNoFilledSlots() {
        String bar = ProgressBar.getProgressBar(0.0, 100.0, 'X', 5, "F", "S", "E");
        assertFalse(bar.contains("F"), "No slot should be fully filled at zero progress");
    }

    @Test
    @DisplayName("2-arg overload defaults maxProgress to 100")
    void testDefaultMaxOverload() {
        assertEquals(ProgressBar.getProgressBar(42.0, 100.0), ProgressBar.getProgressBar(42.0));
    }

    @Test
    @DisplayName("3-arg overload defaults to the box-unicode character")
    void testDefaultCharOverload() {
        String bar = ProgressBar.getProgressBar(30.0, 100.0, 8);
        assertTrue(bar.chars().anyMatch(c -> c == ProgressBar.BOX_UNICODE));
    }
}
