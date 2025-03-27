package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.Interval;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CAInterval implements ConfigAdapter<Interval> {
    @Override
    public Class<Interval> getAdaptType() {
        return Interval.class;
    }

    @Override
    public @Nullable Interval toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        String value = entry.getPrimitive().toString();
        return Interval.fromString(value);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Interval object) {
        return ConfigPrimitive.ofString(object.toSerialString());
    }
}
