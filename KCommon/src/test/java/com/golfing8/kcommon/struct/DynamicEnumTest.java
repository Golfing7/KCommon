package com.golfing8.kcommon.struct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DynamicEnumTest {

    static final class Color extends DynamicEnum<Color> {
        Color(String id) {
            super(id);
        }
    }

    @AfterEach
    void tearDown() {
        DynamicEnum.clearRegistry(Color.class);
    }

    @Test
    void testRegisteredConstantIsFindableByName() {
        Color red = new Color("RED");
        assertEquals("RED", red.name());

        Optional<Color> found = DynamicEnum.valueOf(Color.class, "RED");
        assertTrue(found.isPresent());
        assertSame(red, found.get());
    }

    @Test
    void testUnknownConstantReturnsEmpty() {
        assertFalse(DynamicEnum.valueOf(Color.class, "UNKNOWN").isPresent());
    }

    @Test
    void testValuesReturnsAllRegisteredConstants() {
        Color red = new Color("RED");
        Color blue = new Color("BLUE");

        assertEquals(2, DynamicEnum.values(Color.class).size());
        assertTrue(DynamicEnum.values(Color.class).containsValue(red));
        assertTrue(DynamicEnum.values(Color.class).containsValue(blue));
    }

    @Test
    void testClearRegistryRemovesAllConstants() {
        new Color("RED");
        DynamicEnum.clearRegistry(Color.class);
        assertTrue(DynamicEnum.values(Color.class).isEmpty());
    }

    @Test
    void testReRegisteringSameIdOverwrites() {
        Color first = new Color("RED");
        Color second = new Color("RED");

        Optional<Color> found = DynamicEnum.valueOf(Color.class, "RED");
        assertSame(second, found.get());
        assertNotSame(first, found.get());
    }
}
