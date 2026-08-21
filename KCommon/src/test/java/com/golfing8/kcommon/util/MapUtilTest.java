package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapUtilTest {

    @Test
    @DisplayName("of() builds a map from a single pair")
    void testOfSinglePair() {
        Map<String, Integer> map = MapUtil.of("a", 1);
        assertEquals(1, map.size());
        assertEquals(1, map.get("a"));
    }

    @Test
    @DisplayName("of() builds a map from multiple key/value pairs")
    void testOfMultiplePairs() {
        Map<String, Integer> map = MapUtil.of("a", 1, "b", 2, "c", 3);
        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(3, map.get("c"));
    }

    @Test
    @DisplayName("ofStrict() builds a homogeneously typed map")
    void testOfStrict() {
        Map<String, String> map = MapUtil.ofStrict("a", "1", "b", "2");
        assertEquals("1", map.get("a"));
        assertEquals("2", map.get("b"));
    }

    @Test
    @DisplayName("fill() populates an existing map and returns it")
    void testFill() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("existing", 0);

        Map<String, Integer> result = MapUtil.fill(map, "a", 1, "b", 2);

        assertEquals(map, result, "fill() should return the same map instance's contents");
        assertEquals(3, map.size());
        assertEquals(0, map.get("existing"));
        assertEquals(1, map.get("a"));
        assertEquals(2, map.get("b"));
    }
}
