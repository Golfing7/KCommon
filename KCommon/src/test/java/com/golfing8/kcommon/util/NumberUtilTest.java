package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NumberUtilTest {

    @Test
    @DisplayName("intsToLong/mswInt/lswInt round trip arbitrary ints")
    void testRoundTrip() {
        int[] samples = {0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE, 12345, -98765};
        for (int msw : samples) {
            for (int lsw : samples) {
                long packed = NumberUtil.intsToLong(msw, lsw);
                assertEquals(msw, NumberUtil.mswInt(packed), "msw mismatch for (" + msw + "," + lsw + ")");
                assertEquals(lsw, NumberUtil.lswInt(packed), "lsw mismatch for (" + msw + "," + lsw + ")");
            }
        }
    }

    @Test
    @DisplayName("intsToLong is consistent with LongHash's equivalent packing")
    void testConsistentWithLongHash() {
        long packed = NumberUtil.intsToLong(42, -7);
        assertEquals(LongHash.toLong(42, -7), packed);
        assertEquals(LongHash.msw(packed), NumberUtil.mswInt(packed));
        assertEquals(LongHash.lsw(packed), NumberUtil.lswInt(packed));
    }
}
