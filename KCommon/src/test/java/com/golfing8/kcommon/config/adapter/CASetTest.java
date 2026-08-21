package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CASetTest {

    enum Suit { HEARTS, SPADES, CLUBS, DIAMONDS }

    private static class Holder {
        Set<String> strings;
        Set<Suit> suits;
    }

    private static FieldType fieldType(String name) throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField(name);
        return new FieldType(field);
    }

    @Test
    @DisplayName("Round trips a set of strings")
    void testRoundTripsStringSet() throws NoSuchFieldException {
        Set<String> original = new LinkedHashSet<>(Arrays.asList("a", "b", "c"));
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Set<String> result = ConfigTypeRegistry.getFromType(primitive, fieldType("strings"));
        assertEquals(original, result);
    }

    @Test
    @DisplayName("A null primitive produces an empty set")
    void testNullPrimitiveProducesEmptySet() throws NoSuchFieldException {
        Set<String> result = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofNull(), fieldType("strings"));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("The '@universe' macro expands to all enum constants for enum sets")
    void testUniverseMacroExpandsAllEnumConstants() throws NoSuchFieldException {
        ConfigPrimitive primitive = ConfigPrimitive.ofList(Arrays.asList("@universe"));
        Set<Suit> result = ConfigTypeRegistry.getFromType(primitive, fieldType("suits"));
        assertEquals(new LinkedHashSet<>(Arrays.asList(Suit.values())), result);
    }
}
