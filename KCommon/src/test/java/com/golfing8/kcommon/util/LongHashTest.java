package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LongHashTest {

    @Test
    @DisplayName("packShorts/unpackShorts round trip four shorts in order")
    void testShortRoundTrip() {
        short[] values = {(short) 1, (short) -2, (short) 3, (short) -4};
        long packed = LongHash.packShorts(values[0], values[1], values[2], values[3]);
        assertArrayEquals(values, LongHash.unpackShorts(packed));
    }

    @Test
    @DisplayName("packShorts/unpackShorts handle boundary short values")
    void testShortRoundTripBounds() {
        short[] values = {Short.MIN_VALUE, Short.MAX_VALUE, (short) 0, (short) -1};
        long packed = LongHash.packShorts(values[0], values[1], values[2], values[3]);
        assertArrayEquals(values, LongHash.unpackShorts(packed));
    }

    @Test
    @DisplayName("toLong/msw/lsw round trip arbitrary ints")
    void testIntRoundTrip() {
        int[] samples = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 555, -555};
        for (int msw : samples) {
            for (int lsw : samples) {
                long packed = LongHash.toLong(msw, lsw);
                assertEquals(msw, LongHash.msw(packed));
                assertEquals(lsw, LongHash.lsw(packed));
            }
        }
    }
}
