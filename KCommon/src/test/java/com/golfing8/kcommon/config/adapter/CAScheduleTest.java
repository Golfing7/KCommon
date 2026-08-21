package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.time.Schedule;
import com.golfing8.kcommon.struct.time.Timestamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAScheduleTest {

    @Test
    @DisplayName("Deserializes plain timestamp strings into schedule entries")
    void testDeserializesPlainTimestamps() {
        ConfigPrimitive primitive = ConfigPrimitive.ofList(Arrays.asList("12:00", "18:30"));
        Schedule schedule = ConfigTypeRegistry.getFromType(primitive, Schedule.class);

        List<Timestamp> timestamps = schedule.getAllTimestamps();
        assertEquals(2, timestamps.size());
        assertEquals(Timestamp.parse("12:00").toConfigString(), timestamps.get(0).toConfigString());
        assertEquals(Timestamp.parse("18:30").toConfigString(), timestamps.get(1).toConfigString());
    }

    @Test
    @DisplayName("Deserializes '@' prefixed entries as anticipation times")
    void testDeserializesAnticipationTimes() {
        ConfigPrimitive primitive = ConfigPrimitive.ofList(Arrays.asList("@1m"));
        Schedule schedule = ConfigTypeRegistry.getFromType(primitive, Schedule.class);

        assertEquals(1, schedule.getAnticipationTimes().size());
        assertEquals(1200, schedule.getAnticipationTimes().get(0).getDurationTicks());
    }

    @Test
    @DisplayName("Deserializes '#' prefixed entries as an every-hour recurrence")
    void testDeserializesHourlyRecurrence() {
        ConfigPrimitive primitive = ConfigPrimitive.ofList(Arrays.asList("#15:00"));
        Schedule schedule = ConfigTypeRegistry.getFromType(primitive, Schedule.class);

        assertEquals(Timestamp.everyHour(15, 0).size(), schedule.getAllTimestamps().size());
    }

    @Test
    @DisplayName("Serializes timestamps and anticipation times back into their string forms")
    void testSerializesTimestampsAndAnticipationTimes() {
        Schedule schedule = new Schedule(
                Arrays.asList(Timestamp.parse("12:00")),
                Arrays.asList(com.golfing8.kcommon.struct.time.TimeLength.from(1, java.util.concurrent.TimeUnit.MINUTES))
        );

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(schedule);
        List<String> serialized = primitive.unwrap();
        assertEquals(2, serialized.size());
        assertEquals(Timestamp.parse("12:00").toConfigString(), serialized.get(0));
        assertEquals("@1m", serialized.get(1));
    }
}
