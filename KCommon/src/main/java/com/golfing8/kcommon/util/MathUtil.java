package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public final class MathUtil {
    /**
     * Rounds the given decimal up or down weighted by the decimal part of the number.
     * <p>
     * e.g. Rounding {@code 4.56}: 56% chance to round up, 44% chance to round down.
     * </p>
     * @param d the decimal
     * @return the rounded integer.
     */
    public static int roundRandomly(double d) {
        int minimum = (int) Math.floor(d);
        double difference = d - minimum;
        if (minimum == d)
            return minimum;

        if (ThreadLocalRandom.current().nextDouble() < difference)
            return minimum;
        else
            return minimum + 1;
    }
}
