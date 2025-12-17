package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.time.TimeLength;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adapts instances of {@link TimeLength}
 */
public class CATimeLength implements ConfigAdapter<TimeLength> {
    @Override
    public Class<TimeLength> getAdaptType() {
        return TimeLength.class;
    }

    @Override
    public @Nullable TimeLength toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Object obj = entry.getPrimitive();
        if (obj instanceof Number) {
            return new TimeLength(((Number) obj).longValue());
        } else {
            return TimeLength.parseTime(obj.toString());
        }
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull TimeLength object) {
        return ConfigPrimitive.ofString(object.getAsString(true));
    }
}
