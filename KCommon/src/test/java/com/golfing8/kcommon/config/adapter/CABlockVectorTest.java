package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import org.bukkit.util.BlockVector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CABlockVectorTest {

    @Test
    @DisplayName("Serializes coordinates as doubles (Vector#getX/Y/Z), not ints")
    void testSerializesAsDoubles() {
        BlockVector vector = new BlockVector(1, 2, 3);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(vector);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) primitive.getPrimitive();
        assertEquals(1.0, map.get("x"));
        assertEquals(2.0, map.get("y"));
        assertEquals(3.0, map.get("z"));
    }

    @Test
    @DisplayName("Round-trips a vector through the registry")
    void testRoundTrip() {
        BlockVector vector = new BlockVector(1, 2, 3);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(vector);
        BlockVector result = ConfigTypeRegistry.getFromType(primitive, BlockVector.class);
        assertEquals(1, result.getBlockX());
        assertEquals(2, result.getBlockY());
        assertEquals(3, result.getBlockZ());
    }

    @Test
    @DisplayName("Defaults missing coordinates to 0")
    void testDefaultsMissingCoordinates() {
        CABlockVector adapter = new CABlockVector();
        BlockVector loaded = adapter.toPOJO(ConfigPrimitive.ofMap(java.util.Collections.singletonMap("x", 5)), null);
        assertEquals(5, loaded.getBlockX());
        assertEquals(0, loaded.getBlockY());
        assertEquals(0, loaded.getBlockZ());
    }
}
