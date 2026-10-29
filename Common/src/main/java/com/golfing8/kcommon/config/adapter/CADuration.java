package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.time.TimeLength;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

/**
 * Adapts instances of {@link java.time.Duration}
 */
public class CADuration implements ConfigAdapter<Duration> {
    @Override
    public Class<Duration> getAdaptType() {
        return Duration.class;
    }

    @Override
    public @Nullable Duration toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        if (entry.getPrimitive() instanceof Number) {
            return Duration.ofMillis(((Number) entry.getPrimitive()).longValue());
        }

        String value = entry.getPrimitive().toString();
        TimeLength timeLength = TimeLength.parseTime(value);
        if (timeLength == null)
            return null;

        return Duration.ofMillis(timeLength.getDurationTicks() * 50L);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Duration object) {
        TimeLength length = new TimeLength(object.toMillis() / 50L);
        return ConfigPrimitive.ofString(length.toString());
    }
}
