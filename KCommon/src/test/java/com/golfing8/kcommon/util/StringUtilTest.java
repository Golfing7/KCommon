package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilTest {

    @Nested
    @DisplayName("Case conversion")
    class CaseConversion {
        @Test
        @DisplayName("camelToYaml converts camelCase to yaml-cased-string")
        void testCamelToYaml() {
            assertEquals("some-camel-case", StringUtil.camelToYaml("someCamelCase"));
            assertEquals("simple", StringUtil.camelToYaml("simple"));
        }

        @Test
        @DisplayName("enumToYaml converts ENUM_CASE to yaml-cased-string")
        void testEnumToYaml() {
            assertEquals("some-enum-value", StringUtil.enumToYaml("SOME_ENUM_VALUE"));
            assertEquals("dotted.value", StringUtil.enumToYaml("DOTTED$VALUE"));
        }

        @Test
        @DisplayName("capitalize title-cases each word and lowercases the rest")
        void testCapitalize() {
            assertEquals("This Phrase", StringUtil.capitalize("this phrASe"));
            assertEquals("Under Score", StringUtil.capitalize("under_score"));
        }
    }

    @Nested
    @DisplayName("stripSuffixes")
    class StripSuffixes {
        @Test
        @DisplayName("Strips the first matching suffix")
        void testStripsMatchingSuffix() {
            assertEquals("hello", StringUtil.stripSuffixes("hello.txt", ".txt", ".yml"));
        }

        @Test
        @DisplayName("Returns the original string when no suffix matches")
        void testNoMatchingSuffix() {
            assertEquals("hello.json", StringUtil.stripSuffixes("hello.json", ".txt", ".yml"));
        }
    }

    @Nested
    @DisplayName("Number/comma formatting")
    class NumberFormatting {
        @Test
        @DisplayName("parseCommas(int) inserts thousands separators")
        void testParseCommasInt() {
            assertEquals("1,000,000", StringUtil.parseCommas(1_000_000));
        }

        @Test
        @DisplayName("parseCommas(long) inserts thousands separators")
        void testParseCommasLong() {
            assertEquals("1,000,000,000", StringUtil.parseCommas(1_000_000_000L));
        }

        @Test
        @DisplayName("parseCommas(String) preserves a decimal remainder")
        void testParseCommasStringWithDecimal() {
            assertEquals("1,234.56", StringUtil.parseCommas("1234.56"));
        }

        @Test
        @DisplayName("parseCommas(String) returns short strings unchanged")
        void testParseCommasStringShort() {
            assertEquals("123", StringUtil.parseCommas("123"));
        }

        @Test
        @DisplayName("parseMoney formats with two decimal places")
        void testParseMoney() {
            assertEquals("1,234.5", StringUtil.parseMoney(1234.5));
        }
    }

    @Nested
    @DisplayName("Duration formatting")
    class DurationFormatting {
        @Test
        @DisplayName("timeFormatted with dayFirst=false orders units smallest-first (seconds, minutes, hours, days)")
        void testTimeFormattedSecondsFirst() {
            assertEquals("0s", StringUtil.timeFormatted(0, false));
            assertEquals("5s", StringUtil.timeFormatted(5, false));
            assertEquals("5s 1m", StringUtil.timeFormatted(65, false));
            assertEquals("0s 0m 1h", StringUtil.timeFormatted(3600, false));
        }

        @Test
        @DisplayName("timeFormatted with dayFirst=true orders units largest-first (days, hours, minutes, seconds)")
        void testTimeFormattedDayFirst() {
            assertEquals("1d 0h 0m 0s", StringUtil.timeFormatted(86400, true));
        }

        @Test
        @DisplayName("timeFormattedNoSeconds with dayFirst=false orders units smallest-first (minutes, hours, days)")
        void testTimeFormattedNoSeconds() {
            assertEquals("0m", StringUtil.timeFormattedNoSeconds(0, false));
            assertEquals("5m 1h", StringUtil.timeFormattedNoSeconds(65, false));
        }

        @Test
        @DisplayName("timeFormattedOptionalSeconds only shows seconds when there's no larger unit")
        void testTimeFormattedOptionalSeconds() {
            assertEquals("30s", StringUtil.timeFormattedOptionalSeconds(30));
            assertEquals("1m", StringUtil.timeFormattedOptionalSeconds(60));
        }

        @Test
        @DisplayName("timeFormattedPotion formats as 0:SS when minutes == 0")
        void testTimeFormattedPotionZeroMinutes() {
            assertEquals("00:00", StringUtil.timeFormattedPotion(0));
            assertEquals("0:05", StringUtil.timeFormattedPotion(5));
        }

        @Test
        @DisplayName("timeFormattedPotion formats as M:SS once minutes are non-zero")
        void testTimeFormattedPotionWithMinutes() {
            assertEquals("2:05", StringUtil.timeFormattedPotion(125));
        }
    }

    @Nested
    @DisplayName("Empty checks")
    class EmptyChecks {
        @Test
        @DisplayName("isEmpty is true for null and zero-length sequences")
        void testIsEmpty() {
            assertTrue(StringUtil.isEmpty(null));
            assertTrue(StringUtil.isEmpty(""));
            assertFalse(StringUtil.isEmpty("a"));
        }

        @Test
        @DisplayName("isNotEmpty is the inverse of isEmpty")
        void testIsNotEmpty() {
            assertFalse(StringUtil.isNotEmpty(null));
            assertFalse(StringUtil.isNotEmpty(""));
            assertTrue(StringUtil.isNotEmpty("a"));
        }
    }

    @Nested
    @DisplayName("Roman numerals")
    class RomanNumerals {
        @Test
        @DisplayName("Converts standard integers to roman numerals")
        void testToRoman() {
            assertEquals("I", StringUtil.toRoman(1));
            assertEquals("IV", StringUtil.toRoman(4));
            assertEquals("IX", StringUtil.toRoman(9));
            assertEquals("XIV", StringUtil.toRoman(14));
            assertEquals("MCMXCIV", StringUtil.toRoman(1994));
        }

        @Test
        @DisplayName("Handles zero and a negative number that isn't an exact numeral match")
        void testToRomanZeroAndNegative() {
            assertEquals("0", StringUtil.toRoman(0));
            assertEquals("-III", StringUtil.toRoman(-3));
        }

        @Test
        @DisplayName("Preserves the '-' sign when |number| exactly matches a numeral value")
        void testToRomanNegativeExactMatch() {
            assertEquals("-IV", StringUtil.toRoman(-4));
        }
    }

    @Nested
    @DisplayName("Levenshtein distance")
    class Levenshtein {
        @Test
        @DisplayName("Identical strings have zero distance")
        void testIdentical() {
            assertEquals(0, StringUtil.levenshteinDistance("kitten", "kitten"));
        }

        @Test
        @DisplayName("Matches the classic kitten/sitting example")
        void testKittenSitting() {
            assertEquals(3, StringUtil.levenshteinDistance("kitten", "sitting"));
        }

        @Test
        @DisplayName("Distance against an empty string equals the other string's length")
        void testAgainstEmpty() {
            assertEquals(5, StringUtil.levenshteinDistance("hello", ""));
            assertEquals(5, StringUtil.levenshteinDistance("", "hello"));
        }
    }
}
