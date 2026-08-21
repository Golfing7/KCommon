package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.exc.InvalidConfigException;
import com.golfing8.kcommon.menu.shape.LayoutShapeOutline;
import com.golfing8.kcommon.menu.shape.LayoutShapePoints;
import com.golfing8.kcommon.menu.shape.LayoutShapeRectangle;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.menu.shape.MenuLayoutShape;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CALayoutShapeTest {

    @Test
    @DisplayName("Round trips a rectangle shape, preserving its bounding low/high slots")
    void testRoundTripRectangle() {
        LayoutShapeRectangle shape = new LayoutShapeRectangle(new MenuCoordinate(1, 1), new MenuCoordinate(3, 2));
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive((MenuLayoutShape) shape);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) primitive.getPrimitive();
        assertEquals("RECTANGLE", map.get("type"));

        MenuLayoutShape loaded = ConfigTypeRegistry.getFromType(primitive, MenuLayoutShape.class);
        assertEquals(shape.getInRange(), loaded.getInRange());
    }

    @Test
    @DisplayName("Round trips an outline shape")
    void testRoundTripOutline() {
        LayoutShapeOutline shape = new LayoutShapeOutline(new MenuCoordinate(1, 1), new MenuCoordinate(3, 3));
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive((MenuLayoutShape) shape);

        MenuLayoutShape loaded = ConfigTypeRegistry.getFromType(primitive, MenuLayoutShape.class);
        assertEquals(shape.getInRange(), loaded.getInRange());
    }

    @Test
    @DisplayName("Round trips a points shape")
    void testRoundTripPoints() {
        LayoutShapePoints shape = new LayoutShapePoints(Arrays.asList(new MenuCoordinate(1, 1), new MenuCoordinate(5, 5)));
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive((MenuLayoutShape) shape);

        MenuLayoutShape loaded = ConfigTypeRegistry.getFromType(primitive, MenuLayoutShape.class);
        assertEquals(Arrays.asList(new MenuCoordinate(1, 1), new MenuCoordinate(5, 5)), loaded.getInRange());
    }

    @Test
    @DisplayName("Throws when the type key is missing")
    void testMissingTypeThrows() {
        CALayoutShape adapter = new CALayoutShape();
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(java.util.Collections.emptyMap());
        assertThrows(InvalidConfigException.class, () -> adapter.toPOJO(primitive, null));
    }

    @Test
    @DisplayName("Throws for an unrecognized layout type")
    void testUnrecognizedTypeThrows() {
        CALayoutShape adapter = new CALayoutShape();
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(java.util.Collections.singletonMap("type", "NOT_A_TYPE"));
        assertThrows(InvalidConfigException.class, () -> adapter.toPOJO(primitive, null));
    }
}
