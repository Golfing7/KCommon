package com.golfing8.kcommon.struct.region;

import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RectangleRegionTest {

    @Nested
    @DisplayName("Bounds and area")
    class Bounds {
        @Test
        @DisplayName("Computes area on the X-Z plane")
        void testArea() {
            RectangleRegion region = new RectangleRegion(0, 10, 0, 5);
            assertEquals(50.0, region.getArea());
        }

        @Test
        @DisplayName("Volume is always zero for a plane")
        void testVolume() {
            RectangleRegion region = new RectangleRegion(0, 10, 0, 5);
            assertEquals(0.0, region.getVolume());
        }

        @Test
        @DisplayName("Computes center on the X-Z plane, ignoring Y")
        void testCenter() {
            RectangleRegion region = new RectangleRegion(0, 10, 0, 10);
            assertEquals(new BlockVector(5, 0, 5), region.getCenter());
        }
    }

    @Nested
    @DisplayName("Position containment and distance")
    class PositionChecks {
        private final RectangleRegion region = new RectangleRegion(0, 10, 0, 10);

        @Test
        @DisplayName("Any Y value is within the region as long as X/Z match")
        void testYIsUnbounded() {
            assertTrue(region.isPositionWithin(new Vector(5, -10000, 5)));
            assertTrue(region.isPositionWithin(new Vector(5, 10000, 5)));
        }

        @Test
        @DisplayName("Position outside X/Z bounds returns false")
        void testOutside() {
            assertFalse(region.isPositionWithin(new Vector(11, 0, 5)));
        }

        @Test
        @DisplayName("Distance ignores the Y axis")
        void testDistance() {
            assertEquals(0.0, region.getDistance(new Vector(5, 1000, 5)));
            assertEquals(3.0, region.getDistance(new Vector(13, 0, 5)), 1e-9);
        }
    }

    @Nested
    @DisplayName("Overlap checks")
    class Overlap {
        @Test
        @DisplayName("Overlapping rectangles are detected")
        void testOverlaps() {
            RectangleRegion a = new RectangleRegion(0, 10, 0, 10);
            RectangleRegion b = new RectangleRegion(5, 15, 5, 15);
            assertTrue(a.overlaps(b));
        }

        @Test
        @DisplayName("Non-overlapping rectangles are detected")
        void testNoOverlap() {
            RectangleRegion a = new RectangleRegion(0, 10, 0, 10);
            RectangleRegion b = new RectangleRegion(20, 30, 20, 30);
            assertFalse(a.overlaps(b));
        }
    }

    @Test
    @DisplayName("Shift offsets X and Z but ignores Y")
    void testShift() {
        RectangleRegion region = new RectangleRegion(0, 10, 0, 10);
        Region shifted = region.shift(new Vector(1, 100, 3));
        assertEquals(1, shifted.getMinimumXValue());
        assertEquals(11, shifted.getMaximumXValue());
        assertEquals(3, shifted.getMinimumZValue());
        assertEquals(13, shifted.getMaximumZValue());
    }

    @Test
    @DisplayName("Iterator is unsupported since a rectangle is unbounded on Y")
    void testIteratorUnsupported() {
        RectangleRegion region = new RectangleRegion(0, 10, 0, 10);
        assertThrows(UnsupportedOperationException.class, region::iterator);
    }
}
