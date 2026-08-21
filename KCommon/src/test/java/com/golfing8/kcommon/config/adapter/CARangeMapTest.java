package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.map.RangeMap;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CARangeMapTest {

    private static class Holder {
        RangeMap<String> map;
    }

    private static FieldType fieldType() throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField("map");
        return new FieldType(field);
    }

    @Test
    @DisplayName("Round trips a range-keyed map")
    void testRoundTripsRangeMap() throws NoSuchFieldException {
        RangeMap<String> original = new RangeMap<>();
        original.put(new Range(0, 10), "low");
        original.put(new Range(11, 20), "high");

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        RangeMap<String> result = ConfigTypeRegistry.getFromType(primitive, fieldType());

        assertEquals("low", result.get(5));
        assertEquals("high", result.get(15));
    }
}
