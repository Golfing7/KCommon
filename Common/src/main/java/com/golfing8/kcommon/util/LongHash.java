package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;

/**
 * Contains methods for packing and unpacking numbers into longs.
 */
@UtilityClass
public class LongHash {
    /**
     * Pack four shorts into a single long
     *
     * @param s1 short one MSW
     * @param s2 short two
     * @param s3 short three
     * @param s4 short four LSW
     * @return the packed lopng
     */
    public static long packShorts(short s1, short s2, short s3, short s4) {
        return ((s1 & 0xFFFFL) << 48) | ((s2 & 0xFFFFL) << 32) | ((s3 & 0xFFFFL) << 16) | (s4 & 0xFFFFL);
    }

    /**
     * Unpacks the long into four shorts
     *
     * @param packed the packed long
     * @return the short array, MSW first LSW last
     */
    public static short[] unpackShorts(long packed) {
        short[] data = new short[4];
        long mask = 0xFFFFL;
        data[0] = (short) ((packed >> 48) & mask);
        data[1] = (short) ((packed >> 32) & mask);
        data[2] = (short) ((packed >> 16) & mask);
        data[3] = (short) (packed & mask);
        return data;
    }

    /**
     * Packs the two ints into a long
     *
     * @param msw the msw
     * @param lsw the lsw
     * @return the packed long
     */
    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + (long) lsw - -2147483648L;
    }

    /**
     * Unpacks the MSW int from the long
     *
     * @param l the long
     * @return the msw int
     */
    public static int msw(long l) {
        return (int) (l >> 32);
    }

    /**
     * Unpacks the LSW int from the long
     *
     * @param l the long
     * @return the lsw int
     */
    public static int lsw(long l) {
        return (int) l + -2147483648;
    }
}
