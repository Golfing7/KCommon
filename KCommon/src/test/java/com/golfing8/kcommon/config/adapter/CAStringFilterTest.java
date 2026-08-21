package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.filter.StringFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CAStringFilterTest {

    @Test
    @DisplayName("A simple filter serializes to a bare string")
    void testSimpleFilterSerializesToString() {
        StringFilter filter = new StringFilter("DIAMOND");
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(filter);
        assertEquals("DIAMOND", primitive.getPrimitive());
    }

    @Test
    @DisplayName("Deserializing a bare string yields a simple exact-match filter")
    void testDeserializeBareString() {
        ConfigPrimitive primitive = ConfigPrimitive.ofString("DIAMOND");
        StringFilter result = ConfigTypeRegistry.getFromType(primitive, StringFilter.class);
        assertEquals("DIAMOND", result.getPattern());
        assertTrue(result.isSimple());
    }

    @Test
    @DisplayName("A non-simple filter serializes to a map with all of its flags")
    void testNonSimpleFilterSerializesToMap() {
        StringFilter filter = new StringFilter("dia.*", true, true, true);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(filter);

        Map<String, Object> unwrapped = primitive.unwrap();
        assertEquals(true, unwrapped.get("ignore-case"));
        assertEquals(true, unwrapped.get("contains"));
        assertEquals(true, unwrapped.get("regex"));
        assertEquals("dia.*", unwrapped.get("pattern"));
    }

    @Test
    @DisplayName("Round trips a non-simple filter through a map")
    void testRoundTripsNonSimpleFilter() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("ignore-case", true);
        map.put("contains", true);
        map.put("regex", false);
        map.put("pattern", "dia");
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(map);

        StringFilter result = ConfigTypeRegistry.getFromType(primitive, StringFilter.class);
        assertTrue(result.isIgnoreCase());
        assertTrue(result.isContains());
        assertFalse(result.isRegex());
        assertEquals("dia", result.getPattern());
    }
}
