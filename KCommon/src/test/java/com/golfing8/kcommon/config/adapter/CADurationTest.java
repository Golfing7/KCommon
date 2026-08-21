package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CADurationTest {

    @Test
    @DisplayName("Round trips a duration that falls on a tick boundary (50ms)")
    void testRoundTripsTickAlignedDuration() {
        Duration original = Duration.ofMillis(1000);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Duration result = ConfigTypeRegistry.getFromType(primitive, Duration.class);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("A numeric primitive is interpreted directly as milliseconds")
    void testNumericPrimitiveIsMillis() {
        ConfigPrimitive primitive = ConfigPrimitive.ofInt(5000);
        Duration result = ConfigTypeRegistry.getFromType(primitive, Duration.class);
        assertEquals(Duration.ofMillis(5000), result);
    }

    @Test
    @DisplayName("A duration not aligned to a 50ms tick loses precision on round trip")
    void testSubTickPrecisionIsLost() {
        Duration original = Duration.ofMillis(1025);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Duration result = ConfigTypeRegistry.getFromType(primitive, Duration.class);
        // 1025ms / 50 = 20 ticks (integer division), * 50 = 1000ms, not 1025ms.
        assertEquals(Duration.ofMillis(1000), result);
    }

    @Test
    @DisplayName("A null primitive deserializes to null")
    void testNullPrimitiveIsNull() {
        assertNull(ConfigTypeRegistry.getFromType(ConfigPrimitive.ofNull(), Duration.class));
    }
}
