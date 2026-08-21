package com.golfing8.kcommon.struct.spiral;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SpiralHolderTest {

    @Test
    void testFirstCoordinateIsOrigin() {
        SpiralHolder holder = new SpiralHolder();
        assertEquals(new SpiralCoordinate(0, 0), holder.next());
    }

    @Test
    void testPeekNextDoesNotAdvance() {
        SpiralHolder holder = new SpiralHolder();
        SpiralCoordinate peeked = holder.peekNext();
        SpiralCoordinate polled = holder.next();

        assertEquals(peeked, polled);
        // Peeking again after advancing shows a different coordinate.
        assertNotEquals(polled, holder.peekNext());
    }

    @Test
    void testAllCoordinatesInFirstNStepsAreUnique() {
        SpiralHolder holder = new SpiralHolder();
        Set<SpiralCoordinate> seen = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            SpiralCoordinate coord = holder.next();
            assertTrue(seen.add(coord), "Duplicate coordinate produced: " + coord);
        }
    }

    @Test
    void testSecondRingImmediatelyFollowsOrigin() {
        SpiralHolder holder = new SpiralHolder();
        holder.next(); // (0, 0)

        // The spiral immediately bumps out to radius 1 on the second call.
        SpiralCoordinate second = holder.next();
        assertEquals(1, Math.max(Math.abs(second.getX()), Math.abs(second.getZ())));
    }
}
