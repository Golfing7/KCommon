package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.time.Schedule;
import com.golfing8.kcommon.struct.time.Timestamp;

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
    @SuppressWarnings("unchecked")
    public Schedule toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        List<String> timestampsString = (List<String>) entry.unwrap();
        List<Timestamp> allTimestamps = new ArrayList<>();
        timestampsString.forEach(str -> allTimestamps.add(Timestamp.parse(str)));
        return new Schedule(allTimestamps);
    }

    @Override
    public ConfigPrimitive toPrimitive(Schedule object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        List<String> strings = new ArrayList<>();
        for (Timestamp timestamp : object.getAllTimestamps()) {
            strings.add(timestamp.toConfigString());
        }
        return ConfigPrimitive.ofList(strings);
    }
}
