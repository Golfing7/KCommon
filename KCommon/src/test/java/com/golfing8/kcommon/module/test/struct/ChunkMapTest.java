package com.golfing8.kcommon.module.test.struct;

import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.struct.map.ChunkStylePointMap;
import com.golfing8.kcommon.struct.map.UnboundedCSPointMap;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests chunk-style point maps.
 */
public class ChunkMapTest {
    /**
     * Tests an unbounded chunk map with static values.
     */
    @Test
    public void testUnboundedChunkMapStatic() {
        Map<Position, Integer> values = new LinkedHashMap<>();
        values.put(new Position(-5109, 50, 14815), 0);
        values.put(new Position(-59, 1, -15), 1);
        values.put(new Position(6234, -23, 892), 2);
        values.put(new Position(1058, 250, -5), 3);
        values.put(new Position(100, 6, -508), 4);
        values.put(new Position(0, 0, 0), 5);

        testValues(values, new UnboundedCSPointMap<>());
    }

    /**
     * Tests an unbounded chunk map with dynamic values.
     */
    @Test
    public void testUnboundedChunkMapDynamic() {
        Map<Position, Integer> values = new LinkedHashMap<>();
        for (int i = 0; i <= 15; i++) {
            values.put(new Position(
                    ThreadLocalRandom.current().nextInt(-15000, 15000),
                    ThreadLocalRandom.current().nextInt(-64, 320),
                    ThreadLocalRandom.current().nextInt(-15000, 15000)
            ), i);
        }

        testValues(values, new UnboundedCSPointMap<>());
    }

    /**
     * Verifies that values, lookups, and entries work for a chunk-style map.
     *
     * @param values the values to insert
     * @param chunkMap the map to test
     */
    private void testValues(Map<Position, Integer> values, ChunkStylePointMap<Integer> chunkMap) {
        // Place in some values
        chunkMap.putAll(values);

        // Ensure that all values are properly mapped
        for (Map.Entry<Position, Integer> pair : values.entrySet()) {
            assertEquals(pair.getValue(), chunkMap.get(pair.getKey()), "Map lookup should match");
        }

        // Ensure iterator works properly
        int matched = 0;
        for (Integer value : chunkMap.values()) {
            assertTrue(values.containsValue(value));
            matched++;
        }
        assertEquals(values.size(), matched, "Should match every value");

        // Ensure entries work
        for (Map.Entry<Position, Integer> entry : chunkMap.entrySet()) {
            assertTrue(values.containsKey(entry.getKey()), "Should give correct entries");
            assertEquals(values.get(entry.getKey()), entry.getValue());
        }
    }
}
