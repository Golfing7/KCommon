package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathExpressionsTest {

    @Test
    @DisplayName("Evaluates basic arithmetic expressions")
    void testBasicArithmetic() {
        assertEquals(4.0, MathExpressions.evaluate("2 + 2"));
        assertEquals(50.0, MathExpressions.evaluate("10 * 5"));
        assertEquals(2.0, MathExpressions.evaluate("10 / 5"));
    }

    @Test
    @DisplayName("Substitutes placeholders before evaluating")
    void testPlaceholderSubstitution() {
        assertEquals(15.0, MathExpressions.evaluate("{AMOUNT} + 5", "AMOUNT", 10));
    }

    @Test
    @DisplayName("rand0() produces a value between 0 and 1")
    void testRand0Bounds() {
        for (int i = 0; i < 50; i++) {
            double value = MathExpressions.evaluate("rand0()");
            assertTrue(value >= 0.0 && value < 1.0);
        }
    }

    @Test
    @DisplayName("rand1(bound) produces a value between 0 and the bound")
    void testRand1Bounds() {
        for (int i = 0; i < 50; i++) {
            double value = MathExpressions.evaluate("rand1(10)");
            assertTrue(value >= 0.0 && value < 10.0);
        }
    }

    @Test
    @DisplayName("rand2(min, max) produces a value between min and max")
    void testRand2Bounds() {
        for (int i = 0; i < 50; i++) {
            double value = MathExpressions.evaluate("rand2(5, 10)");
            assertTrue(value >= 5.0 && value < 10.0);
        }
    }
}
