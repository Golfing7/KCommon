package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.blocks.WeightedCollection;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAWeightedCollectionTest {

    private static class Holder {
        WeightedCollection<String> strings;
    }

    private static FieldType fieldType() throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField("strings");
        return new FieldType(field);
    }

    @Test
    @DisplayName("Round trips a weighted collection's entries and weights")
    void testRoundTripsWeightedCollection() throws NoSuchFieldException {
        WeightedCollection<String> original = new WeightedCollection<>();
        original.addWeightedObject("common", 70.0);
        original.addWeightedObject("rare", 30.0);

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        WeightedCollection<String> result = ConfigTypeRegistry.getFromType(primitive, fieldType());

        Map<String, Double> resultMap = result.getChanceMap();
        assertEquals(70.0, resultMap.get("common"));
        assertEquals(30.0, resultMap.get("rare"));
    }
}
