package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.time.TimeLength;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CATimeLengthTest {

    @Test
    @DisplayName("Round trips a time length through its string form")
    void testRoundTripsAsString() {
        TimeLength original = new TimeLength(1315);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        TimeLength result = ConfigTypeRegistry.getFromType(primitive, TimeLength.class);
        assertEquals(original.getDurationTicks(), result.getDurationTicks());
    }

    @Test
    @DisplayName("A numeric primitive is interpreted directly as tick count")
    void testNumericPrimitiveIsTickCount() {
        ConfigPrimitive primitive = ConfigPrimitive.ofInt(100);
        TimeLength result = ConfigTypeRegistry.getFromType(primitive, TimeLength.class);
        assertEquals(100, result.getDurationTicks());
    }

    @Test
    @DisplayName("Parses shorthand duration strings like '1m 30s'")
    void testParsesShorthandString() {
        ConfigPrimitive primitive = ConfigPrimitive.ofString("1m 30s");
        TimeLength result = ConfigTypeRegistry.getFromType(primitive, TimeLength.class);
        assertEquals(1800, result.getDurationTicks());
    }

    @Test
    @DisplayName("A null primitive deserializes to null")
    void testNullPrimitiveIsNull() {
        assertNull(ConfigTypeRegistry.getFromType(ConfigPrimitive.ofNull(), TimeLength.class));
    }
}
