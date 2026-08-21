package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CAMapTest {

    private static class Holder {
        Map<String, Integer> stringToInt;
    }

    private static FieldType fieldType() throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField("stringToInt");
        return new FieldType(field);
    }

    @Test
    @DisplayName("Round trips a map of string keys to adapted integer values")
    void testRoundTripsMap() throws NoSuchFieldException {
        Map<String, Integer> original = new LinkedHashMap<>();
        original.put("a", 1);
        original.put("b", 2);

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Map<String, Integer> result = ConfigTypeRegistry.getFromType(primitive, fieldType());
        assertEquals(original, result);
    }

    @Test
    @DisplayName("A null primitive produces an empty map")
    void testNullPrimitiveProducesEmptyMap() throws NoSuchFieldException {
        Map<String, Integer> result = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofNull(), fieldType());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("A non-string key is coerced to its string representation via toString()")
    void testNonStringKeyIsCoercedToString() {
        Map<Integer, Integer> intKeyedMap = new LinkedHashMap<>();
        intKeyedMap.put(1, 100);

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(intKeyedMap);
        Map<String, Object> unwrapped = primitive.unwrap();
        assertEquals(100, unwrapped.get("1"));
    }
}
