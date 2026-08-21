package com.golfing8.kcommon.struct.time;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TimeLengthTest {

    @Test
    void testNegativeDurationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new TimeLength(-1));
    }

    @Test
    void testToUnitConversions() {
        // 20 ticks per second.
        TimeLength length = new TimeLength(20L * 60 * 60 * 24); // 1 day worth of ticks
        assertEquals(1, length.toUnit(TimeUnit.DAYS));
        assertEquals(24, length.toUnit(TimeUnit.HOURS));
        assertEquals(24 * 60, length.toUnit(TimeUnit.MINUTES));
        assertEquals(24 * 60 * 60, length.toUnit(TimeUnit.SECONDS));
    }

    @Test
    void testToUnitMillisecondsFromTicks() {
        TimeLength length = new TimeLength(20L);
        assertEquals(1000L, length.toUnit(TimeUnit.MILLISECONDS));
    }

    @Test
    void testFromDurationAndUnitRoundTrips() {
        TimeLength length = TimeLength.from(5, TimeUnit.SECONDS);
        assertEquals(100L, length.getDurationTicks());
    }

    @Test
    void testGetAsStringWithoutTicks() {
        // 1 day, 1 hour, 1 minute, 1 second, 5 ticks.
        long ticks = 1728000L + 72000L + 1200L + 20L + 5L;
        TimeLength length = new TimeLength(ticks);
        assertEquals("1d 1h 1m 1s", length.getAsString(false));
    }

    @Test
    void testGetAsStringWithTicks() {
        long ticks = 1728000L + 72000L + 1200L + 20L + 5L;
        TimeLength length = new TimeLength(ticks);
        assertEquals("1d 1h 1m 1s 5t", length.getAsString(true));
    }

    @Test
    void testGetAsStringOmitsZeroComponents() {
        TimeLength length = new TimeLength(20L * 90); // 1m 30s
        assertEquals("1m 30s", length.getAsString(false));
    }

    @Test
    void testToStringUsesStringRepresentationWithoutTicks() {
        TimeLength length = new TimeLength(25L); // 1s 5t
        assertEquals("1s", length.toString());
    }

    @Test
    void testCompareTo() {
        TimeLength shorter = new TimeLength(10);
        TimeLength longer = new TimeLength(20);

        assertTrue(shorter.compareTo(longer) < 0);
        assertTrue(longer.compareTo(shorter) > 0);
        assertEquals(0, shorter.compareTo(new TimeLength(10)));
    }

    @Test
    void testParseTimeSingleUnit() {
        TimeLength parsed = TimeLength.parseTime("5s");
        assertEquals(100L, parsed.getDurationTicks());
    }

    @Test
    void testParseTimeMultipleUnits() {
        TimeLength parsed = TimeLength.parseTime("1d2h3m4s");
        long expected = 1 * 20L * 86400L + 2 * 20L * 3600L + 3 * 20L * 60L + 4 * 20L;
        assertEquals(expected, parsed.getDurationTicks());
    }

    @Test
    void testParseTimeWithWhitespaceAndCommas() {
        TimeLength parsed = TimeLength.parseTime("1d, 2h, 3m");
        long expected = 1 * 20L * 86400L + 2 * 20L * 3600L + 3 * 20L * 60L;
        assertEquals(expected, parsed.getDurationTicks());
    }

    @Test
    void testParseTimeTicksUnit() {
        TimeLength parsed = TimeLength.parseTime("15t");
        assertEquals(15L, parsed.getDurationTicks());
    }

    @Test
    void testParseTimeUnrecognizedUnitReturnsNull() {
        assertNull(TimeLength.parseTime("5x"));
    }

    @Test
    void testParseTimeWhitespaceMidNumberReturnsNull() {
        assertNull(TimeLength.parseTime("5 s"));
    }
}
