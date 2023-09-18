package com.golfing8.kcommon.module.test;

import com.golfing8.kcommon.struct.time.Timestamp;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
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
    }
}
