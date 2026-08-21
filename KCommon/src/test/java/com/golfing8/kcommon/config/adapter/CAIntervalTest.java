package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.Interval;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CAIntervalTest {

    @Test
    @DisplayName("Round trips an interval with a custom step")
    void testRoundTripsIntervalWithStep() {
        Interval original = new Interval(1, 5, 2);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        assertEquals("1;2;5", primitive.getPrimitive());

        Interval result = ConfigTypeRegistry.getFromType(primitive, Interval.class);
        assertEquals(1.0, result.getX1());
        assertEquals(5.0, result.getX2());
        assertEquals(2.0, result.getInterval());
    }

    @Test
    @DisplayName("Round trips an interval with the default step")
    void testRoundTripsIntervalDefaultStep() {
        Interval original = new Interval(1, 5, 1);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        assertEquals("1;5", primitive.getPrimitive());

        Interval result = ConfigTypeRegistry.getFromType(primitive, Interval.class);
        assertEquals(1.0, result.getX1());
        assertEquals(5.0, result.getX2());
        assertEquals(1.0, result.getInterval());
    }

    @Test
    @DisplayName("A null primitive deserializes to null")
    void testNullPrimitiveIsNull() {
        assertNull(ConfigTypeRegistry.getFromType(ConfigPrimitive.ofNull(), Interval.class));
    }
}
