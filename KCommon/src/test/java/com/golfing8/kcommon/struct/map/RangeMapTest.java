package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.struct.Range;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangeMapTest {

    @Test
    void testPutAndGetWithinRange() {
        RangeMap<String> map = new RangeMap<>();
        map.put(new Range(0, 10), "low");
        map.put(new Range(11, 20), "high");

        assertEquals("low", map.get(5.0));
        assertEquals("high", map.get(15.0));
    }

    @Test
    void testGetOutsideAnyRangeReturnsNull() {
        RangeMap<String> map = new RangeMap<>();
        map.put(new Range(0, 10), "low");

        assertNull(map.get(50.0));
    }

    @Test
    void testGetBoundaryValuesAreInclusive() {
        RangeMap<String> map = new RangeMap<>();
        map.put(new Range(0, 10), "low");

        assertEquals("low", map.get(0.0));
        assertEquals("low", map.get(10.0));
    }

    @Test
    void testDoubleKeyPutOverload() {
        RangeMap<String> map = new RangeMap<>();
        map.put(5.0, "exact");

        assertEquals("exact", map.get(5.0));
        assertNull(map.get(5.1));
    }

    @Test
    void testRemoveByNumberKey() {
        RangeMap<String> map = new RangeMap<>();
        map.put(new Range(0, 10), "low");

        String removed = map.remove(Double.valueOf(5.0));
        assertEquals("low", removed);
        assertNull(map.get(5.0));
    }

    @Test
    void testMinimumAndMaximumKeys() {
        RangeMap<String> map = new RangeMap<>();
        map.put(new Range(0, 10), "low");
        map.put(new Range(20, 30), "high");

        assertEquals(0.0, map.getMinimumKey());
        assertEquals(20.0, map.getMaximumKey());
    }

    @Test
    void testIsEmptyAndSize() {
        RangeMap<String> map = new RangeMap<>();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());

        map.put(new Range(0, 10), "low");
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
    }

    @Test
    void testBuilderConstructsPopulatedMap() {
        RangeMap<String> map = RangeMap.<String>builder()
                .put(new Range(0, 10), "low")
                .put(new Range(11, 20), "high")
                .build();

        assertEquals("low", map.get(3.0));
        assertEquals("high", map.get(15.0));
    }

    @Test
    void testClearRemovesAllEntries() {
        RangeMap<String> map = new RangeMap<>();
        map.put(new Range(0, 10), "low");
        map.clear();

        assertTrue(map.isEmpty());
        assertNull(map.get(5.0));
    }
}
