package com.golfing8.kcommon.struct.time;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleTest {

    @Test
    void testGetAllTimestampsReturnsRegisteredEntries() {
        Timestamp morning = Timestamp.ofIntraDay(9, 0, 0);
        Timestamp evening = Timestamp.ofIntraDay(21, 0, 0);
        Schedule schedule = new Schedule(Arrays.asList(morning, evening));

        List<Timestamp> timestamps = schedule.getAllTimestamps();
        assertEquals(2, timestamps.size());
        assertTrue(timestamps.contains(morning));
        assertTrue(timestamps.contains(evening));
    }

    @Test
    void testAddTimeAppendsToSchedule() {
        Schedule schedule = new Schedule(Collections.emptyList());
        Timestamp noon = Timestamp.ofIntraDay(12, 0, 0);

        schedule.addTime(noon);

        assertEquals(Collections.singletonList(noon), schedule.getAllTimestamps());
    }

    @Test
    void testCheckNextTimestampFiresOnlyAfterEntryPasses() {
        Timestamp entry = Timestamp.ofIntraDay(12, 0, 0);
        Schedule schedule = new Schedule(Collections.singletonList(entry));

        // Before the entry, nothing should fire, but it should be armed.
        Timestamp before = Timestamp.ofIntraDay(11, 59, 0);
        assertNull(schedule.checkNextTimestamp(before));

        // After the entry passes, it should fire exactly once.
        Timestamp after = Timestamp.ofIntraDay(12, 0, 1);
        assertEquals(entry, schedule.checkNextTimestamp(after));

        // Calling again at the same "after" time should not re-fire (armed flag consumed).
        assertNull(schedule.checkNextTimestamp(after));
    }

    @Test
    void testCheckNextTimestampRearmsAfterCyclingBeforeAgain() {
        Timestamp entry = Timestamp.ofIntraDay(12, 0, 0);
        Schedule schedule = new Schedule(Collections.singletonList(entry));

        Timestamp before = Timestamp.ofIntraDay(11, 0, 0);
        Timestamp after = Timestamp.ofIntraDay(12, 0, 1);

        // A freshly-constructed entry is not "armed" - polling while already past the entry does
        // not fire until a poll from before the entry is observed first.
        assertNull(schedule.checkNextTimestamp(after));

        // Polling from before the entry arms it.
        assertNull(schedule.checkNextTimestamp(before));

        // Now that it's armed, polling after the entry fires it.
        assertEquals(entry, schedule.checkNextTimestamp(after));

        // Time moves back before the entry (e.g. next day) - it re-arms.
        assertNull(schedule.checkNextTimestamp(before));

        assertEquals(entry, schedule.checkNextTimestamp(after));
    }

    @Test
    void testAnticipationTimesConstructorStoresLengths() {
        Timestamp entry = Timestamp.ofIntraDay(12, 0, 0);
        TimeLength length = new TimeLength(100);
        Schedule schedule = new Schedule(Collections.singletonList(entry), Collections.singletonList(length));

        assertEquals(Collections.singletonList(length), schedule.getAnticipationTimes());
    }
}
