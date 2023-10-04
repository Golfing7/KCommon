package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.reflection.FieldType;

public class CARange implements ConfigAdapter<Range> {
    @Override
    public Class<Range> getAdaptType() {
        return Range.class;
    }

    @Override
    public Range toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        String[] splitValue = ((String) entry.getPrimitive()).split(":");
        double minimum = Double.parseDouble(splitValue[0]);
        double maximum = Double.parseDouble(splitValue[1]);
        return new Range(minimum, maximum);
    }

    @Override
    public ConfigPrimitive toPrimitive(Range object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        return ConfigPrimitive.ofString(object.getMin() + ":" + object.getMax());
    }
}
