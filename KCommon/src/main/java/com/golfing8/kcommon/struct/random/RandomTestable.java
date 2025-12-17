package com.golfing8.kcommon.struct.random;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An interface that defines
 */
public interface RandomTestable {
    /**
     * Gets the chance of SUCCESS (true) for this class.
     * This number should be on the range of 0-100
     *
     * @return the chance of success.
     */
    double getChance();

    /**
     * Gets the instance of random to use.
     *
     * @return the instance of random.
     */
    default Random getRandomInstance() {
        return ThreadLocalRandom.current();
    }

    /**
     * Tests the random instance with default odds
     *
     * @return true if success
     */
    default boolean testRandom() {
        return getChance() > getRandomInstance().nextDouble() * 100.0D;
    }

    /**
     * Tests the random with modified odds
     *
     * @param boost the boost
     * @return true if success
     */
    default boolean testRandom(double boost) {
        return getChance() * boost > getRandomInstance().nextDouble() * 100.0D;
    }
}
