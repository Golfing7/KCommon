package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapts instances of {@link Range}
 */
public class CARange implements ConfigAdapter<Range> {
    private static final double EPSILON = 1e-7;
    // Matches two (possibly negative) numbers, optionally separated by one of |;:- . The leading
    // '-' of a number is consumed by the number group itself rather than treated as a separator,
    // so negative bounds (e.g. "-5-10") parse correctly instead of colliding with the separator.
    private static final Pattern RANGE_PATTERN = Pattern.compile("^(-?\\d+(?:\\.\\d+)?)(?:[|;:-](-?\\d+(?:\\.\\d+)?))?$");

    @Override
    public Class<Range> getAdaptType() {
        return Range.class;
    }

    @Override
    public Range toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        String value = ConfigPrimitive.coerceBoxedToString(entry.unwrap());
        Matcher matcher = RANGE_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range value: " + value);
        }

        double minimum = Double.parseDouble(matcher.group(1));
        // If there's only one number, just interpret it as a single point.
        if (matcher.group(2) == null) {
            return new Range(minimum, minimum);
        }
        double maximum = Double.parseDouble(matcher.group(2));
        return new Range(minimum, maximum);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Range object) {
        // Check if we should encode it with ints instead of floats. (Makes the config easier to read and work with)
        long minRounded = Math.round(object.getMin());
        long maxRounded = Math.round(object.getMax());
        double minDifference = Math.abs(minRounded - object.getMin());
        double maxDifference = Math.abs(maxRounded - object.getMax());
        if (minDifference <= EPSILON && maxDifference <= EPSILON) {
            if (minRounded == maxRounded) {
                return ConfigPrimitive.ofString(String.valueOf(minRounded));
            }

            return ConfigPrimitive.ofString(minRounded + "-" + maxRounded);
        }

        if (Math.abs(object.getMin() - object.getMax()) <= EPSILON) {
            return ConfigPrimitive.ofString(String.valueOf(object.getMin()));
        }
        return ConfigPrimitive.ofString(object.getMin() + "-" + object.getMax());
    }
}
