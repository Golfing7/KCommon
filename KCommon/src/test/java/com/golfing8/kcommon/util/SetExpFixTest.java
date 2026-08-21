package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Only the pure, Player-independent static math ({@link SetExpFix#getExpAtLevel(int)} and
 * {@link SetExpFix#getExpToLevel(int)}) is covered here. The player-mutating methods
 * (addTotalExperience/takeTotalExperience/setTotalExperience/getTotalExperience/
 * getExpUntilNextLevel) drive real vanilla level-up behavior through {@code Player#giveExp}, which
 * a Mockito mock can't faithfully reproduce and this project's FakeServer/FakeWorld test doubles
 * don't provide a spawnable Player for. Exercising those would require a live CraftPlayer.
 */
class SetExpFixTest {

    @Nested
    @DisplayName("getExpAtLevel")
    class GetExpAtLevel {
        @Test
        @DisplayName("Uses the 1.8 vanilla piecewise formula")
        void testFormula() {
            assertEquals(7, SetExpFix.getExpAtLevel(0));
            assertEquals(9, SetExpFix.getExpAtLevel(1));
            assertEquals(37, SetExpFix.getExpAtLevel(15));
        }

        @Test
        @DisplayName("Matches the documented breakpoints at level 15/16 and 30/31")
        void testBreakpoints() {
            assertEquals((2 * 15) + 7, SetExpFix.getExpAtLevel(15));
            assertEquals((5 * 16) - 38, SetExpFix.getExpAtLevel(16));
            assertEquals((5 * 30) - 38, SetExpFix.getExpAtLevel(30));
            assertEquals((9 * 31) - 158, SetExpFix.getExpAtLevel(31));
        }
    }

    @Nested
    @DisplayName("getExpToLevel")
    class GetExpToLevel {
        @Test
        @DisplayName("Level 0 requires no experience")
        void testLevelZero() {
            assertEquals(0, SetExpFix.getExpToLevel(0));
        }

        @Test
        @DisplayName("Is the running sum of getExpAtLevel for all prior levels")
        void testCumulativeSum() {
            int expected = 0;
            for (int level = 0; level < 10; level++) {
                assertEquals(expected, SetExpFix.getExpToLevel(level));
                expected += SetExpFix.getExpAtLevel(level);
            }
        }

        @Test
        @DisplayName("Is monotonically increasing and cache-consistent across repeated calls")
        void testMonotonicAndCached() {
            int previous = -1;
            for (int level = 0; level <= 50; level++) {
                int value = SetExpFix.getExpToLevel(level);
                assertEquals(value, SetExpFix.getExpToLevel(level), "Repeated call should be stable/cached");
                assertTrue(value >= previous, "getExpToLevel should never decrease");
                previous = value;
            }
        }
    }
}
