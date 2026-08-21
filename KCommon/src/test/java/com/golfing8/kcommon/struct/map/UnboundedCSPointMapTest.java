package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.nms.struct.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnboundedCSPointMapTest {

    @Test
    void testPutAndGet() {
        UnboundedCSPointMap<String> map = new UnboundedCSPointMap<>();
        Position pos = new Position(5.0, 5.0, 5.0);

        assertNull(map.put(pos, "value"));
        assertEquals("value", map.get(pos));
        assertEquals(1, map.size());
    }

    @Test
    void testGetOnMissingChunkReturnsNull() {
        UnboundedCSPointMap<String> map = new UnboundedCSPointMap<>();
        Position pos = new Position(5.0, 5.0, 5.0);

        assertNull(map.get(pos));
    }

    @Test
    void testPutReplacesExistingValue() {
        UnboundedCSPointMap<String> map = new UnboundedCSPointMap<>();
        Position pos = new Position(5.0, 5.0, 5.0);

        map.put(pos, "first");
        String old = map.put(pos, "second");

        assertEquals("first", old);
        assertEquals("second", map.get(pos));
        assertEquals(1, map.size());
    }

    @Test
    void testRemoveEmptiesBackingChunkColumn() {
        UnboundedCSPointMap<String> map = new UnboundedCSPointMap<>();
        Position pos = new Position(5.0, 5.0, 5.0);
        map.put(pos, "value");

        String removed = map.remove(pos);

        assertEquals("value", removed);
        assertEquals(0, map.size());
        assertNull(map.get(pos));
        // The chunk column backing this position should have been evicted once empty.
        assertNull(map.getChunkColumn(pos));
    }

    @Test
    void testUnboundedRangeSupportsArbitraryFarPositions() {
        UnboundedCSPointMap<String> map = new UnboundedCSPointMap<>();
        Position near = new Position(0.0, 0.0, 0.0);
        Position far = new Position(1_000_000.0, 0.0, 1_000_000.0);

        map.put(near, "near");
        map.put(far, "far");

        assertEquals("near", map.get(near));
        assertEquals("far", map.get(far));
        assertEquals(2, map.size());
    }

    @Test
    void testClearRemovesAllChunkColumns() {
        UnboundedCSPointMap<String> map = new UnboundedCSPointMap<>();
        Position pos = new Position(5.0, 5.0, 5.0);
        map.put(pos, "value");

        map.clear();

        assertNull(map.get(pos));
    }

    @Test
    void testGetChunkColumnByChunkCoordinates() {
        UnboundedCSPointMap<String> map = new UnboundedCSPointMap<>();
        Position pos = new Position(5.0, 5.0, 5.0);
        map.put(pos, "value");

        assertNotNull(map.getChunkColumn(0, 0));
        assertNull(map.getChunkColumn(5, 5));
    }
}
