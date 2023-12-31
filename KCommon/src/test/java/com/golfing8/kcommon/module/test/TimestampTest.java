package com.golfing8.kcommon.module.test;

import com.golfing8.kcommon.struct.time.Timestamp;
import static org.junit.jupiter.api.Assertions.*;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests some internals of timestamps
 */
public class TimestampTest {
    @Test
    public void testTimestampDifferences() {
        Timestamp past = Timestamp.parse("12:00:00");
        Timestamp future = Timestamp.parse("19:00:00");
        assertEquals(future.getMillisDifference(past), TimeUnit.HOURS.toMillis(7));

        past = Timestamp.parse("01:59:59");
        future = Timestamp.parse("2:00:00");
        assertEquals(future.getMillisDifference(past), TimeUnit.SECONDS.toMillis(1));
    }
}
