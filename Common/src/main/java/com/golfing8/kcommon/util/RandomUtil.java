package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Provides some utilities for randomness
 */
@UtilityClass
public class RandomUtil {
    /**
     * Tests the chance at the given level using the thread local random
     *
     * @param chance the chance
     * @return true if success
     */
    public static boolean testChance(double chance) {
        return testChance(ThreadLocalRandom.current(), chance);
    }

    /**
     * Tests the chance at the given level using the given random instance
     *
     * @param random the random
     * @param chance the chance
     * @return true if success
     */
    public static boolean testChance(Random random, double chance) {
        return chance > random.nextDouble() * 100.0D;
    }
}
