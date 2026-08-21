package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CAOptionalTest {

    private static class Holder {
        Optional<Integer> optionalInt;
    }

    private static FieldType fieldType() throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField("optionalInt");
        return new FieldType(field);
    }

    @Test
    @DisplayName("Round trips a present optional value")
    void testRoundTripsPresentValue() throws NoSuchFieldException {
        Optional<Integer> original = Optional.of(42);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Optional<Integer> result = ConfigTypeRegistry.getFromType(primitive, fieldType());
        assertTrue(result.isPresent());
        assertEquals(42, result.get());
    }

    @Test
    @DisplayName("An empty optional serializes to null and deserializes back to empty")
    void testRoundTripsEmptyOptional() throws NoSuchFieldException {
        Optional<Integer> original = Optional.empty();
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Optional<Integer> result = ConfigTypeRegistry.getFromType(primitive, fieldType());
        assertFalse(result.isPresent());
    }
}
