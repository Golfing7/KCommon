package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CAListTest {

    private static class Holder {
        List<Integer> ints;
        List<String> strings;
    }

    private static FieldType fieldType(String name) throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField(name);
        return new FieldType(field);
    }

    @Test
    @DisplayName("Round trips a list of primitive-adapted integers")
    void testRoundTripsIntList() throws NoSuchFieldException {
        List<Integer> original = Arrays.asList(1, 2, 3);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        List<Integer> result = ConfigTypeRegistry.getFromType(primitive, fieldType("ints"));
        assertEquals(original, result);
    }

    @Test
    @DisplayName("Round trips a list of strings")
    void testRoundTripsStringList() throws NoSuchFieldException {
        List<String> original = Arrays.asList("a", "b", "c");
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        List<String> result = ConfigTypeRegistry.getFromType(primitive, fieldType("strings"));
        assertEquals(original, result);
    }

    @Test
    @DisplayName("An empty/null primitive produces an empty list")
    void testNullPrimitiveProducesEmptyList() throws NoSuchFieldException {
        ConfigPrimitive primitive = ConfigPrimitive.ofNull();
        List<Integer> result = ConfigTypeRegistry.getFromType(primitive, fieldType("ints"));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("A singleton (non-list) value is treated as a single-element list")
    void testSingletonValueBecomesSingleElementList() throws NoSuchFieldException {
        ConfigPrimitive primitive = ConfigPrimitive.ofTrusted(5);
        List<Integer> result = ConfigTypeRegistry.getFromType(primitive, fieldType("ints"));
        assertEquals(Arrays.asList(5), result);
    }
}
