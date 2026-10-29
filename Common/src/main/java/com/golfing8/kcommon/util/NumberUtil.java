package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;

/**
 * Various number utilities
 */
@UtilityClass
public final class NumberUtil {
    /**
     * Gets the hash of both integers.
     *
     * @param msw the first number.
     * @param lsw the second number.
     * @return the hash.
     */
    public static long intsToLong(int msw, int lsw) {
        return ((long) msw << 32) + (long) lsw - -2147483648L;
    }

    /**
     * Gets the MSW of the given long.
     *
     * @param l the long.
     * @return the msw.
     */
    public static int mswInt(long l) {
        return (int) (l >> 32);
    }

    /**
     * Gets the LSW of the given long.
     *
     * @param l the long.
     * @return the lsw.
     */
    public static int lswInt(long l) {
        return (int) (l & -1L) + Integer.MIN_VALUE;
    }
}
