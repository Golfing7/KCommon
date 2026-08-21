package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathUtilTest {

    @Test
    @DisplayName("roundRandomly returns exact value for whole numbers")
    void testRoundRandomlyWholeNumber() {
        assertEquals(4, MathUtil.roundRandomly(4.0));
        assertEquals(-3, MathUtil.roundRandomly(-3.0));
    }

    @Test
    @DisplayName("roundRandomly always rounds to floor or ceiling of the value")
    void testRoundRandomlyBounds() {
        for (int i = 0; i < 200; i++) {
            int rounded = MathUtil.roundRandomly(4.25);
            assertTrue(rounded == 4 || rounded == 5);
        }
    }

    @Test
    @DisplayName("clamp(int) restricts value to the given bounds")
    void testClampInt() {
        assertEquals(5, MathUtil.clamp(5, 0, 10));
        assertEquals(0, MathUtil.clamp(-5, 0, 10));
        assertEquals(10, MathUtil.clamp(15, 0, 10));
    }

    @Test
    @DisplayName("clamp(long) restricts value to the given bounds")
    void testClampLong() {
        assertEquals(5L, MathUtil.clamp(5L, 0L, 10L));
        assertEquals(0L, MathUtil.clamp(-5L, 0L, 10L));
        assertEquals(10L, MathUtil.clamp(15L, 0L, 10L));
    }

    @Test
    @DisplayName("clamp(double) restricts value to the given bounds")
    void testClampDouble() {
        assertEquals(5.5, MathUtil.clamp(5.5, 0.0, 10.0));
        assertEquals(0.0, MathUtil.clamp(-5.5, 0.0, 10.0));
        assertEquals(10.0, MathUtil.clamp(15.5, 0.0, 10.0));
    }
}
