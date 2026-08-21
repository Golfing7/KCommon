package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.DynamicEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class CADynamicEnumTest {

    static final class Fruit extends DynamicEnum<Fruit> {
        Fruit(String id) {
            super(id);
        }
    }

    @AfterEach
    void tearDown() {
        DynamicEnum.clearRegistry(Fruit.class);
    }

    @Test
    @DisplayName("Round trips a registered dynamic enum constant by name")
    void testRoundTripsRegisteredConstant() {
        Fruit apple = new Fruit("APPLE");
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(apple);
        assertEquals("APPLE", primitive.getPrimitive());

        Fruit result = ConfigTypeRegistry.getFromType(primitive, Fruit.class);
        assertSame(apple, result);
    }

    @Test
    @DisplayName("An unregistered constant name deserializes to null")
    void testUnregisteredConstantReturnsNull() {
        ConfigPrimitive primitive = ConfigPrimitive.ofString("MISSING");
        Fruit result = ConfigTypeRegistry.getFromType(primitive, Fruit.class);
        assertNull(result);
    }
}
