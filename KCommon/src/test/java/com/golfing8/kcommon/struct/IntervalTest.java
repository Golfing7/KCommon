package com.golfing8.kcommon.struct;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntervalTest {

    private static List<Double> collect(Iterable<Double> iterable) {
        List<Double> values = new ArrayList<>();
        for (double d : iterable) {
            values.add(d);
        }
        return values;
    }

    @Nested
    class Iteration {
        @Test
        void testForwardIterationIncludesBothEndpoints() {
            Interval interval = new Interval(0, 10, 2);
            assertEquals(Arrays.asList(0.0, 2.0, 4.0, 6.0, 8.0, 10.0), collect(interval));
            assertEquals(6, interval.getIntervalSize());
        }

        @Test
        void testBackwardIterationIncludesBothEndpoints() {
            Interval interval = new Interval(10, 0, 2);
            assertEquals(Arrays.asList(10.0, 8.0, 6.0, 4.0, 2.0, 0.0), collect(interval));
            assertEquals(6, interval.getIntervalSize());
        }

        @Test
        void testCapBehaviorStopsAtBoundaryForeverAfter() {
            Interval interval = new Interval(0, 5, 2);
            interval.setOverflowBehavior(Interval.CAP);

            Iterator<Double> iterator = interval.iterator();
            List<Double> firstFive = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                firstFive.add(iterator.next());
            }
            // Once it hits the end (5.0), it should keep returning 5.0 (capped).
            assertEquals(5.0, firstFive.get(firstFive.size() - 1));
            assertEquals(5.0, iterator.next());
            assertEquals(5.0, iterator.next());
        }

        @Test
        void testWrapBehaviorReturnsToStart() {
            Interval interval = new Interval(0, 4, 2);
            interval.setOverflowBehavior(Interval.WRAP);

            Iterator<Double> iterator = interval.iterator();
            assertEquals(0.0, iterator.next());
            assertEquals(2.0, iterator.next());
            assertEquals(4.0, iterator.next());
            // Wraps back to x1.
            assertEquals(0.0, iterator.next());
            assertEquals(2.0, iterator.next());
        }
    }

    @Nested
    class Serialization {
        @Test
        void testToSerialStringWithDefaultInterval() {
            Interval interval = new Interval(1, 5, 1);
            assertEquals("1;5", interval.toSerialString());
        }

        @Test
        void testToSerialStringWithCustomInterval() {
            Interval interval = new Interval(1, 5, 2);
            assertEquals("1;2;5", interval.toSerialString());
        }

        @Test
        void testFromStringTwoPartRoundTrips() {
            Interval interval = Interval.fromString("1;5");
            assertEquals(1.0, interval.getX1());
            assertEquals(5.0, interval.getX2());
            assertEquals(1.0, interval.getInterval());
        }

        @Test
        void testFromStringThreePartRoundTrips() {
            Interval interval = Interval.fromString("1;2;5");
            assertEquals(1.0, interval.getX1());
            assertEquals(2.0, interval.getInterval());
            assertEquals(5.0, interval.getX2());
        }
    }
}
