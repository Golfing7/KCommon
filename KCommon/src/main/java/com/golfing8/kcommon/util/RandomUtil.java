package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class RandomUtil {
    public static boolean testChance(double chance) {
        return testChance(ThreadLocalRandom.current(), chance);
    }

    public static boolean testChance(Random random, double chance) {
        return chance > random.nextDouble() * 100.0D;
    }
}
