package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAZonedDateTimeTest {

    @Test
    @DisplayName("Round trips a zoned date time including its zone")
    void testRoundTripsZonedDateTime() {
        ZonedDateTime original = ZonedDateTime.of(2024, 3, 15, 10, 30, 45, 0, ZoneId.of("UTC"));
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        ZonedDateTime result = ConfigTypeRegistry.getFromType(primitive, ZonedDateTime.class);
        assertEquals(original, result);
    }

    @Test
    @DisplayName("Missing hour/minute/second fields default to zero")
    void testMissingTimeFieldsDefaultToZero() {
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("zone-id", "UTC");
        map.put("year", 2024);
        map.put("month", 1);
        map.put("day", 1);
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(map);

        ZonedDateTime result = ConfigTypeRegistry.getFromType(primitive, ZonedDateTime.class);
        assertEquals(0, result.getHour());
        assertEquals(0, result.getMinute());
        assertEquals(0, result.getSecond());
    }
}
