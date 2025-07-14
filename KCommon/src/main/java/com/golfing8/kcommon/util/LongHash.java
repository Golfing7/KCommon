package com.golfing8.kcommon.util;

public class LongHash {
    public static long packShorts(short s1, short s2, short s3, short s4) {
        return ((s1 & 0xFFFFL) << 48) | ((s2 & 0xFFFFL) << 32) | ((s3 & 0xFFFFL) << 16) | (s4 & 0xFFFFL);
    }

    public static short[] unpackShorts(long packed) {
        short[] data = new short[4];
        long mask = 0xFFFFL;
        data[0] = (short) ((packed >> 48) & mask);
        data[1] = (short) ((packed >> 32) & mask);
        data[2] = (short) ((packed >> 16) & mask);
        data[3] = (short) (packed & mask);
        return data;
    }

    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + (long) lsw - -2147483648L;
    }

    public static int msw(long l) {
        return (int) (l >> 32);
    }

    public static int lsw(long l) {
        return (int) (l & -1L) + -2147483648;
    }


}
