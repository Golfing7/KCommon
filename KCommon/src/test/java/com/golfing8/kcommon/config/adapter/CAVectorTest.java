package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.util.MapUtil;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAVectorTest {

    @Test
    @DisplayName("Round trips a vector through the string format")
    void testRoundTrip() {
        Vector vector = new Vector(1.5, -2.5, 3.0);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(vector);
        assertEquals("1.5:-2.5:3.0", primitive.getPrimitive());

        Vector loaded = ConfigTypeRegistry.getFromType(primitive, Vector.class);
        assertEquals(vector, loaded);
    }

    @Test
    @DisplayName("Reads a vector from a map-based primitive, defaulting missing coordinates to 0")
    void testReadFromMap() {
        CAVector adapter = new CAVector();
        ConfigPrimitive primitive = ConfigPrimitive.ofTrusted(MapUtil.of("x", 5.0, "z", 2.0));

        Vector loaded = adapter.toPOJO(primitive, null);
        assertEquals(5.0, loaded.getX());
        assertEquals(0.0, loaded.getY());
        assertEquals(2.0, loaded.getZ());
    }
}
