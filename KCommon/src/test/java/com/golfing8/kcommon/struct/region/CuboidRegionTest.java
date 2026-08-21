package com.golfing8.kcommon.struct.region;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CuboidRegionTest {

    @Nested
    @DisplayName("Bounds and construction")
    class Bounds {
        @Test
        @DisplayName("Normalizes min/max regardless of constructor argument order")
        void testNormalizesBounds() {
            CuboidRegion region = new CuboidRegion(5, 1, 10, 2, -1, -5);
            assertEquals(1, region.getMinimumXValue());
            assertEquals(5, region.getMaximumXValue());
            assertEquals(2, region.getMinimumYValue());
            assertEquals(10, region.getMaximumYValue());
            assertEquals(-5, region.getMinimumZValue());
            assertEquals(-1, region.getMaximumZValue());
        }

        @Test
        @DisplayName("Computes center as the midpoint of the bounds")
        void testCenter() {
            CuboidRegion region = new CuboidRegion(0, 10, 0, 10, 0, 10);
            assertEquals(new BlockVector(5, 5, 5), region.getCenter());
        }

        @Test
        @DisplayName("Computes volume")
        void testVolume() {
            CuboidRegion region = new CuboidRegion(0, 2, 0, 3, 0, 4);
            assertEquals(24.0, region.getVolume());
        }
    }

    @Nested
    @DisplayName("Position containment and distance")
    class PositionChecks {
        private final CuboidRegion region = new CuboidRegion(0, 10, 0, 10, 0, 10);

        @Test
        @DisplayName("Position within bounds returns true")
        void testWithin() {
            assertTrue(region.isPositionWithin(new Vector(5, 5, 5)));
            assertTrue(region.isPositionWithin(new Vector(0, 0, 0)));
            assertTrue(region.isPositionWithin(new Vector(10, 10, 10)));
        }

        @Test
        @DisplayName("Position outside bounds returns false")
        void testOutside() {
            assertFalse(region.isPositionWithin(new Vector(11, 5, 5)));
            assertFalse(region.isPositionWithin(new Vector(-1, 5, 5)));
        }

        @Test
        @DisplayName("Distance is zero when inside the region")
        void testDistanceInside() {
            assertEquals(0.0, region.getDistance(new Vector(5, 5, 5)));
        }

        @Test
        @DisplayName("Distance is computed correctly when outside the region")
        void testDistanceOutside() {
            // 3 units away on the X axis only
            assertEquals(3.0, region.getDistance(new Vector(13, 5, 5)), 1e-9);
        }
    }

    @Nested
    @DisplayName("Overlap checks")
    class Overlap {
        @Test
        @DisplayName("Overlapping regions are detected")
        void testOverlaps() {
            CuboidRegion a = new CuboidRegion(0, 10, 0, 10, 0, 10);
            CuboidRegion b = new CuboidRegion(5, 15, 5, 15, 5, 15);
            assertTrue(a.overlapsWith(b));
            assertTrue(b.overlapsWith(a));
        }

        @Test
        @DisplayName("Non-overlapping regions are detected")
        void testNoOverlap() {
            CuboidRegion a = new CuboidRegion(0, 10, 0, 10, 0, 10);
            CuboidRegion b = new CuboidRegion(20, 30, 20, 30, 20, 30);
            assertFalse(a.overlapsWith(b));
        }
    }

    @Nested
    @DisplayName("Transformations")
    class Transformations {
        @Test
        @DisplayName("Grow expands bounds on all axes")
        void testGrow() {
            CuboidRegion region = new CuboidRegion(0, 10, 0, 10, 0, 10);
            Region grown = region.grow(5);
            assertEquals(-5, grown.getMinimumXValue());
            assertEquals(15, grown.getMaximumXValue());
        }

        @Test
        @DisplayName("Shift offsets bounds by the given vector")
        void testShift() {
            CuboidRegion region = new CuboidRegion(0, 10, 0, 10, 0, 10);
            Region shifted = region.shift(new Vector(1, 2, 3));
            assertEquals(1, shifted.getMinimumXValue());
            assertEquals(11, shifted.getMaximumXValue());
            assertEquals(2, shifted.getMinimumYValue());
            assertEquals(12, shifted.getMaximumYValue());
            assertEquals(3, shifted.getMinimumZValue());
            assertEquals(13, shifted.getMaximumZValue());
        }
    }

    @Nested
    @DisplayName("Block iteration")
    class Iteration {
        @Test
        @DisplayName("Iterates over every block position exactly once")
        void testIteratesAllBlocks() {
            CuboidRegion region = new CuboidRegion(0, 1, 0, 1, 0, 1);
            Set<BlockVector> visited = new HashSet<>();
            int count = 0;
            for (BlockVector vector : region) {
                visited.add(vector);
                count++;
            }
            assertEquals(8, count); // 2x2x2 cube
            assertEquals(8, visited.size());
            assertTrue(visited.contains(new BlockVector(0, 0, 0)));
            assertTrue(visited.contains(new BlockVector(1, 1, 1)));
        }

        @Test
        @DisplayName("Iterator throws when exhausted")
        void testIteratorExhausted() {
            CuboidRegion region = new CuboidRegion(0, 0, 0, 0, 0, 0);
            java.util.Iterator<BlockVector> it = region.iterator();
            assertTrue(it.hasNext());
            it.next();
            assertFalse(it.hasNext());
            assertThrows(java.util.NoSuchElementException.class, it::next);
        }
    }
}
