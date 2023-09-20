package com.golfing8.kcommon.module.test.struct;

import com.golfing8.kcommon.struct.filter.StringFilter;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

public class StringFilterTest {
    private static String randomString(int min, int max) {
        int length = ThreadLocalRandom.current().nextInt(min, max);
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = (char) ThreadLocalRandom.current().nextInt(36, 127);
        }
        return new String(chars);
    }

    @Test
    public void testSimpleFilter() {
        String pattern = randomString(10, 15);
        StringFilter simpleFilter = new StringFilter(pattern);

        assertTrue(simpleFilter.filter(pattern) != 0);
        assertEquals(0, simpleFilter.filter("Pad " + pattern));
        assertEquals(0, simpleFilter.filter(""));
        assertEquals(0, simpleFilter.filter(null));
    }
}
