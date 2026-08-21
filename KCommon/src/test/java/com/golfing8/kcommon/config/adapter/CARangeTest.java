package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CARangeTest {

    @Test
    @DisplayName("Round trips an integer-valued range as 'min-max'")
    void testRoundTripsIntegerRange() {
        Range original = new Range(1, 10);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        assertEquals("1-10", primitive.getPrimitive());

        Range result = ConfigTypeRegistry.getFromType(primitive, Range.class);
        assertEquals(1.0, result.getMin());
        assertEquals(10.0, result.getMax());
    }

    @Test
    @DisplayName("A single point range serializes as just the number")
    void testSinglePointRange() {
        Range original = new Range(5, 5);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        assertEquals("5", primitive.getPrimitive());

        Range result = ConfigTypeRegistry.getFromType(primitive, Range.class);
        assertEquals(5.0, result.getMin());
        assertEquals(5.0, result.getMax());
    }

    @Test
    @DisplayName("Deserializes a single bare number as a single-point range")
    void testDeserializeSingleNumberString() {
        ConfigPrimitive primitive = ConfigPrimitive.ofString("7");
        Range result = ConfigTypeRegistry.getFromType(primitive, Range.class);
        assertEquals(7.0, result.getMin());
        assertEquals(7.0, result.getMax());
    }

    @Test
    @DisplayName("Round-trips a range with a negative minimum")
    void testNegativeMinimumRoundTrip() {
        Range original = new Range(-5, 10);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        assertEquals("-5-10", primitive.getPrimitive());

        Range roundTripped = ConfigTypeRegistry.getFromType(primitive, Range.class);
        assertEquals(-5.0, roundTripped.getMin());
        assertEquals(10.0, roundTripped.getMax());
    }

    @Test
    @DisplayName("Round-trips a range with a negative minimum and maximum")
    void testNegativeMinimumAndMaximumRoundTrip() {
        Range original = new Range(-10, -5);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);

        Range roundTripped = ConfigTypeRegistry.getFromType(primitive, Range.class);
        assertEquals(-10.0, roundTripped.getMin());
        assertEquals(-5.0, roundTripped.getMax());
    }
}
