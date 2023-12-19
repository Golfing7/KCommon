package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.reflection.FieldType;

public class CARange implements ConfigAdapter<Range> {
    private static final double EPSILON = 1e-7;

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
        // If there's only one number, just interpret it as a single point.
        if (splitValue.length == 1) {
            return new Range(minimum, minimum);
        }
        double maximum = Double.parseDouble(splitValue[1]);
        return new Range(minimum, maximum);
    }

    @Override
    public ConfigPrimitive toPrimitive(Range object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        // Check if we should encode it with ints instead of floats. (Makes the config easier to read and work with)
        long minRounded = Math.round(object.getMin());
        long maxRounded = Math.round(object.getMax());
        double minDifference = Math.abs(minRounded - object.getMin());
        double maxDifference = Math.abs(maxRounded - object.getMax());
        if (minDifference <= EPSILON && maxDifference <= EPSILON) {
            if (minRounded == maxRounded) {
                return ConfigPrimitive.ofString(String.valueOf(minRounded));
            }

            return ConfigPrimitive.ofString(minRounded + ":" + maxRounded);
        }

        if (Math.abs(object.getMin() - object.getMax()) <= EPSILON) {
            return ConfigPrimitive.ofString(String.valueOf(object.getMin()));
        }
        return ConfigPrimitive.ofString(object.getMin() + ":" + object.getMax());
    }
}
