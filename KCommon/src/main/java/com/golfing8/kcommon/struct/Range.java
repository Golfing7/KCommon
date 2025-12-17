package com.golfing8.kcommon.struct;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that represents a range of numbers.
 */
@Data
@RequiredArgsConstructor
public class Range {
    private final double EPSILON = 0.0001D;

    /**
     * The minimum and maximum values of the range.
     */
    private final double min, max;

    public Range(double value) {
        this.min = this.max = value;
    }

    /**
     * Checks if the given integer is within the range.
     *
     * @param i the number to check.
     * @return true if it's within the range.
     */
    public boolean inRange(int i) {
        return min <= i && i <= max;
    }

    /**
     * Checks if the given double is within the range.
     *
     * @param d the number to check.
     * @return true if it's within the range.
     */
    public boolean inRange(double d) {
        return min <= d && d <= max;
    }

    /**
     * Gets the minimum as an integer, rounded down.
     *
     * @return the floor of the minimum
     */
    public int getMinFloor() {
        return (int) Math.floor(this.min);
    }

    /**
     * Gets the maximum as an integer, rounded down.
     *
     * @return the floor of the maximum
     */
    public int getMaxFloor() {
        return (int) Math.floor(this.max);
    }

    /**
     * Gets a random integer within this range, inclusive.
     *
     * @return the random number.
     */
    public int getRandomI() {
        return ThreadLocalRandom.current().nextInt((int) min, (int) max + 1);
    }

    /**
     * Gets a random double within this range, inclusive.
     *
     * @return the random number.
     */
    public double getRandomD() {
        return ThreadLocalRandom.current().nextDouble(min, max + EPSILON);
    }

    /**
     * Converts the min/max into a string in the format {@code min-max}
     *
     * @return the string
     */
    public String toString() {
        if (this.max == this.min)
            return String.valueOf(this.min);

        return this.min + "-" + this.max;
    }

    /**
     * Converts the min/max into ints then into a string in the format {@code min-max}
     *
     * @return the string
     */
    public String toIntString() {
        long min = (long) this.min;
        long max = (long) this.max;
        if (min == max)
            return String.valueOf(min);

        return min + "-" + max;
    }

    /**
     * Generates a range from a given string. The format should follow 'min|max'. Note that negative numbers are not
     * currently supported.
     *
     * @param string the string to parse from.
     * @return the created range.
     */
    public static Range fromString(String string) {
        String[] split = string.split("[:;|]");

        if (split.length == 1) {
            split = new String[]{split[0], split[0]};
        }

        double n1 = Double.parseDouble(split[0]);
        double n2 = Double.parseDouble(split[1]);

        double min = Math.min(n1, n2);
        double max = Math.max(n1, n2);
        return new Range(min, max);
    }
}
