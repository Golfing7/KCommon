package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.util.MapUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAMenuCoordinateTest {

    @Test
    @DisplayName("Round trips a coordinate through its slot number")
    void testRoundTripSlot() {
        MenuCoordinate coordinate = new MenuCoordinate(3, 2);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(coordinate);
        assertEquals(coordinate.toSlot(), primitive.getPrimitive());

        MenuCoordinate loaded = ConfigTypeRegistry.getFromType(primitive, MenuCoordinate.class);
        assertEquals(coordinate, loaded);
    }

    @Test
    @DisplayName("Reads a coordinate from an x/y map primitive")
    void testReadFromMap() {
        CAMenuCoordinate adapter = new CAMenuCoordinate();
        MenuCoordinate loaded = adapter.toPOJO(ConfigPrimitive.ofMap(MapUtil.of("x", 4, "y", 5)), null);
        assertEquals(4, loaded.getX());
        assertEquals(5, loaded.getY());
    }
}
