package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.time.Schedule;
import com.golfing8.kcommon.struct.time.TimeLength;
import com.golfing8.kcommon.struct.time.Timestamp;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A config adapter for {@link Schedule} instances.
 */
public class CASchedule implements ConfigAdapter<Schedule> {
    @Override
    public Class<Schedule> getAdaptType() {
        return Schedule.class;
    }

    @Override
    public Schedule toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        List<String> timestampsString = entry.unwrap();
        List<Timestamp> allTimestamps = new ArrayList<>();
        List<TimeLength> anticipatedTimes = new ArrayList<>();
        timestampsString.forEach(str -> {
            if (str.startsWith("@")) {
                str = str.replace("@", "");
                anticipatedTimes.add(TimeLength.parseTime(str));
            } else {
                allTimestamps.add(Timestamp.parse(str));
            }
        });
        return new Schedule(allTimestamps, anticipatedTimes);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Schedule object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        List<String> strings = new ArrayList<>();
        for (Timestamp timestamp : object.getAllTimestamps()) {
            strings.add(timestamp.toConfigString());
        }
        for (TimeLength length : object.getAnticipationTimes()) {
            strings.add("@" + length.getAsString(true));
        }
        return ConfigPrimitive.ofList(strings);
    }
}
