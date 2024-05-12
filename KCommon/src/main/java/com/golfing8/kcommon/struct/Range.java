package com.golfing8.kcommon.struct;

import lombok.Data;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A class that represents a range of numbers.
 */
@Data
public class Range {
    private final double EPSILON = 0.0001D;

    /**
     * The minimum and maximum values of the range.
     */
    private final double min, max;

    /**
     * Checks if the given integer is within the range.
     *
     * @param i the number to check.
     * @return true if it's within the range.
     */
    public boolean inRange(int i){
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
     * Gets a random integer within this range, inclusive.
     *
     * @return the random number.
     */
    public int getRandomI(){
        return ThreadLocalRandom.current().nextInt((int) min, (int) max + 1);
    }

    /**
     * Gets a random double within this range, inclusive.
     *
     * @return the random number.
     */
    public double getRandomD(){
        return ThreadLocalRandom.current().nextDouble(min, max + EPSILON);
    }

    /**
     * Generates a range from a given string. The format should follow 'min|max'. Note that negative numbers are not
     * currently supported.
     *
     * @param string the string to parse from.
     * @return the created range.
     */
    public static Range fromString(String string){
        String[] split = string.split(";");

        if(split.length == 1){
            split = new String[] {split[0], split[0]};
        }

        double n1 = Double.parseDouble(split[0]);
        double n2 = Double.parseDouble(split[1]);

        double min = Math.min(n1, n2);
        double max = Math.max(n1, n2);
        return new Range(min, max);
    }
}
