package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.struct.region.CuboidRegion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundedCSPointMapTest {

    // A 32x32x32 region (two chunks wide in each horizontal direction).
    private static CuboidRegion region() {
        return new CuboidRegion(0, 31, 0, 31, 0, 31);
    }

    @Test
    void testPutAndGetWithinRegion() {
        BoundedCSPointMap<String> map = new BoundedCSPointMap<>(region());
        Position pos = new Position(5.0, 5.0, 5.0);

        assertNull(map.put(pos, "value"));
        assertEquals("value", map.get(pos));
        assertEquals(1, map.size());
    }

    @Test
    void testPutReplacesExistingValueWithoutGrowingSize() {
        BoundedCSPointMap<String> map = new BoundedCSPointMap<>(region());
        Position pos = new Position(5.0, 5.0, 5.0);

        map.put(pos, "first");
        String old = map.put(pos, "second");

        assertEquals("first", old);
        assertEquals("second", map.get(pos));
        assertEquals(1, map.size());
    }

    @Test
    void testGetOutsideRegionThrows() {
        BoundedCSPointMap<String> map = new BoundedCSPointMap<>(region());
        Position outside = new Position(100.0, 100.0, 100.0);

        assertThrows(IllegalArgumentException.class, () -> map.get(outside));
    }

    @Test
    void testRemoveDecreasesSize() {
        BoundedCSPointMap<String> map = new BoundedCSPointMap<>(region());
        Position pos = new Position(5.0, 5.0, 5.0);
        map.put(pos, "value");

        String removed = map.remove(pos);
        assertEquals("value", removed);
        assertEquals(0, map.size());
        assertNull(map.get(pos));
    }

    @Test
    void testPointsInDifferentChunksAreIndependent() {
        BoundedCSPointMap<String> map = new BoundedCSPointMap<>(region());
        Position chunk0 = new Position(1.0, 1.0, 1.0);
        Position chunk1 = new Position(17.0, 1.0, 1.0);

        map.put(chunk0, "a");
        map.put(chunk1, "b");

        assertEquals("a", map.get(chunk0));
        assertEquals("b", map.get(chunk1));
        assertEquals(2, map.size());
    }

    @Test
    void testClearResetsSizeAndStorage() {
        BoundedCSPointMap<String> map = new BoundedCSPointMap<>(region());
        Position pos = new Position(5.0, 5.0, 5.0);
        map.put(pos, "value");

        map.clear();

        assertEquals(0, map.size());
        assertNull(map.get(pos));
    }

    @Test
    void testGetChunkColumnByChunkCoordinates() {
        BoundedCSPointMap<String> map = new BoundedCSPointMap<>(region());
        Position pos = new Position(5.0, 5.0, 5.0);
        map.put(pos, "value");

        ChunkColumn<String> column = map.getChunkColumn(0, 0);
        assertNotNull(column);
    }
}
