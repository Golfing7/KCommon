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

        if (ThreadLocalRandom.current().nextDouble() >= difference)
            return minimum;
        else
            return minimum + 1;
    }

    /**
     * Clamps the given value between the minimum and maximum provided.
     *
     * @param value the value.
     * @param min the minimum
     * @param max the maximum
     * @return the clamped value
     */
    public static int clamp(int value, int min, int max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * Clamps the given value between the minimum and maximum provided.
     *
     * @param value the value.
     * @param min the minimum
     * @param max the maximum
     * @return the clamped value
     */
    public static long clamp(long value, long min, long max) {
        return value < min ? min : value > max ? max : value;
    }

    /**
     * Clamps the given value between the minimum and maximum provided.
     *
     * @param value the value.
     * @param min the minimum
     * @param max the maximum
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max) {
        return value < min ? min : value > max ? max : value;
    }
}
