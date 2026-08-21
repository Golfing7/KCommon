package com.golfing8.kcommon.struct;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RollingAverageTest {

    @Test
    void testInitialTotalIsZero() {
        RollingAverage average = new RollingAverage(20);
        assertEquals(0.0D, average.getTotal());
    }

    @Test
    void testAddAccumulatesIntoTotal() {
        // size 20 makes the divisor (size / 20D) == 1, simplifying expected math.
        RollingAverage average = new RollingAverage(20);
        average.add(5);
        average.add(3);
        assertEquals(8.0D, average.getTotal());
    }

    @Test
    void testAddOverwritesOldestSampleOnWraparound() {
        RollingAverage average = new RollingAverage(2);
        average.add(1); // samples: [1, 0], total = 1
        average.add(2); // samples: [1, 2], total = 3
        average.add(3); // wraps to index 0, replacing 1 with 3: samples: [3, 2], total = 5

        // divisor is (2 / 20D) = 0.1, so total 5 / 0.1 = 50
        assertEquals(50.0D, average.getTotal());
    }

    @Test
    void testEditHeadAddsWithoutAdvancingIndex() {
        RollingAverage average = new RollingAverage(20);
        average.editHead(4);
        average.editHead(6);
        // Both edits apply to the same head slot and both accumulate into total.
        assertEquals(10.0D, average.getTotal());
    }
}
