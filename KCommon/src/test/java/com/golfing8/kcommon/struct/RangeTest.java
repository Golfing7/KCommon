package com.golfing8.kcommon.struct;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangeTest {

    @Test
    void testInRangeInclusiveBounds() {
        Range range = new Range(1, 5);
        assertTrue(range.inRange(1));
        assertTrue(range.inRange(5));
        assertTrue(range.inRange(3));
        assertFalse(range.inRange(0));
        assertFalse(range.inRange(6));
    }

    @Test
    void testSingleValueConstructor() {
        Range range = new Range(3);
        assertEquals(3, range.getMin());
        assertEquals(3, range.getMax());
    }

    @Test
    void testGetMinMaxFloor() {
        Range range = new Range(1.9, 5.1);
        assertEquals(1, range.getMinFloor());
        assertEquals(5, range.getMaxFloor());
    }

    @Test
    void testGetRandomIIsWithinBounds() {
        Range range = new Range(2, 4);
        for (int i = 0; i < 100; i++) {
            int value = range.getRandomI();
            assertTrue(value >= 2 && value <= 4);
        }
    }

    @Test
    void testGetRandomDIsWithinBounds() {
        Range range = new Range(2, 4);
        for (int i = 0; i < 100; i++) {
            double value = range.getRandomD();
            assertTrue(value >= 2 && value <= 4.001);
        }
    }

    @Test
    void testToStringEqualBounds() {
        Range range = new Range(5, 5);
        assertEquals("5.0", range.toString());
    }

    @Test
    void testToStringDifferentBounds() {
        Range range = new Range(1, 5);
        assertEquals("1.0-5.0", range.toString());
    }

    @Test
    void testToIntStringEqualBounds() {
        Range range = new Range(5, 5);
        assertEquals("5", range.toIntString());
    }

    @Test
    void testToIntStringDifferentBounds() {
        Range range = new Range(1, 5);
        assertEquals("1-5", range.toIntString());
    }

    @Test
    void testFromStringSingleValue() {
        Range range = Range.fromString("5");
        assertEquals(5, range.getMin());
        assertEquals(5, range.getMax());
    }

    @Test
    void testFromStringDashSeparated() {
        Range range = Range.fromString("2-8");
        assertEquals(2, range.getMin());
        assertEquals(8, range.getMax());
    }

    @Test
    void testFromStringNormalizesMinMaxOrder() {
        Range range = Range.fromString("8:2");
        assertEquals(2, range.getMin());
        assertEquals(8, range.getMax());
    }
}
