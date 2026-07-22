package com.golfing8.kcommon.util;

import com.golfing8.kcommon.util.string.StringMacros;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringMacrosTest {

    private StringMacros engine;

    @BeforeEach
    void setUp() {
        engine = StringMacros.DEFAULT;
    }

    @Nested
    @DisplayName("Default Macro Specific Tests")
    class DefaultMacroTests {

        @Test
        @DisplayName("Test lower case $lc{} and upper case $uc{}")
        void testCasing() {
            assertEquals("hello world", engine.parse("$lc{HELLO WORLD}"));
            assertEquals("HELLO WORLD", engine.parse("$uc{hello world}"));
        }

        @Test
        @DisplayName("Test capitalization $cap{}")
        void testCapitalize() {
            assertEquals("Hello World", engine.parse("$cap{hello world}"));
        }

        @Test
        @DisplayName("Test $commas{} and $stripcommas{}")
        void testCommas() {
            assertEquals("1,000,000", engine.parse("$commas{1000000}"));
            assertEquals("1000000", engine.parse("$stripcommas{1,000,000}"));
        }

        @Test
        @DisplayName("Test random string $rs{} generation")
        void testRandomString() {
            String resultDefault = engine.parse("$rs{abc}");
            assertEquals(8, resultDefault.length());
            assertTrue(resultDefault.matches("[abc]+"));

            String resultCustomLength = engine.parse("$rs(4){xyz}");
            assertEquals(4, resultCustomLength.length());
            assertTrue(resultCustomLength.matches("[xyz]+"));

            assertEquals("", engine.parse("$rs{}"));
        }

        @Test
        @DisplayName("Test strip color $sc{}")
        void testStripColor() {
            assertEquals("Red Green", engine.parse("$sc{§cRed §aGreen}"));
        }

        @Test
        @DisplayName("Test roman numeral $roman{} conversion")
        void testRoman() {
            assertEquals("V", engine.parse("$roman{5}"));
            assertEquals("X", engine.parse("$roman{10}"));
        }

        @Test
        @DisplayName("Test string replace $replace(from)(to){text}")
        void testReplace() {
            assertEquals("I love my cat", engine.parse("$replace(dog)(cat){I love my dog}"));
        }

        @Test
        @DisplayName("Test repeat $repeat(count){text}")
        void testRepeat() {
            // Default repeat count (2)
            assertEquals("HiHi", engine.parse("$repeat{Hi}"));
            // Custom repeat count (4)
            assertEquals("HiHiHiHi", engine.parse("$repeat(4){Hi}"));
        }

        @Test
        @DisplayName("Test math evaluation $eval{}")
        void testEval() {
            assertEquals("4", engine.parse("$eval{2+2}"));
            assertEquals("50", engine.parse("$eval{10*5}"));
        }

        @Test
        @DisplayName("Test string reverse $reverse{}")
        void testReverse() {
            assertEquals("gnirtS desreveR", engine.parse("$reverse{Reversed String}"));
        }

        @Test
        @DisplayName("Test string substring $substring(begin)(end){}")
        void testSubstring() {
            assertEquals("racecar", engine.parse("$substring(4)(11){The racecar is very fast}"));
        }

        @Test
        @DisplayName("Tests double formatting as int $int{}")
        void testInt() {
            assertEquals("123", engine.parse("$int{123.3214}"));
            assertEquals("3", engine.parse("$int{3.1415}"));
        }
    }

    @Nested
    @DisplayName("Nesting & Composition Tests")
    class NestingTests {

        @Test
        @DisplayName("Parses nested default macros inside body content")
        void testNestedMacrosInBody() {
            // Inner: $lc{WORLD} -> "world", Outer: $cap{world} -> "World"
            assertEquals("Hello World", engine.parse("Hello $cap{$lc{WORLD}}"));
        }

        @Test
        @DisplayName("Parses nested default macros inside arguments")
        void testNestedMacrosInArguments() {
            // Inner arg: $uc{cat} -> "CAT", Outer replace: "dog" with "CAT"
            String input = "$replace(dog)($uc{cat}){I love my dog}";
            assertEquals("I love my CAT", engine.parse(input));
        }

        @Test
        @DisplayName("Chains multiple default macros sequentially")
        void testChainedMacros() {
            String input = "Result: $eval{2+2}, Code: $uc{$rs(3){a}}, Count: $repeat(2){!}";
            String result = engine.parse(input);
            assertTrue(result.startsWith("Result: 4, Code: AAA, Count: !!"));
        }
    }

    @Nested
    @DisplayName("Edge Cases & Unbalanced Inputs")
    class EdgeCaseTests {

        @Test
        @DisplayName("Handles null and empty string safely")
        void testNullAndEmptyInput() {
            assertNull(engine.parse(null));
            assertEquals("", engine.parse(""));
        }

        @Test
        @DisplayName("Ignores unregistered macro symbols")
        void testUnregisteredMacro() {
            assertEquals("This $unknown{text} remains.", engine.parse("This $unknown{text} remains."));
        }

        @Test
        @DisplayName("Gracefully handles missing braces or unbalanced brackets")
        void testUnbalancedBrackets() {
            assertEquals("$uc{hello world", engine.parse("$uc{hello world"));
            assertEquals("$repeat(3{hello}", engine.parse("$repeat(3{hello}"));
        }

        @Test
        @DisplayName("Handles empty content braces")
        void testEmptyContentBraces() {
            assertEquals("", engine.parse("$uc{}"));
        }
    }

    @Test
    @DisplayName("Benchmark: Parser processes >100k operations/second")
    void testPerformanceThroughput() {
        StringMacros engine = new StringMacros();
        engine.registerMacro("uc", (args, string) -> string.toUpperCase());
        engine.registerMacro("replace", (args, string) -> {
            if (args.size() < 2) return string;
            return string.replace(args.get(0), args.get(1));
        });

        String template = "Hello $uc{world}! $replace(cat)(dog){I love my cat!}";

        // Warm-up phase to let JIT compiler optimize hot paths
        for (int i = 0; i < 10_000; i++) {
            engine.parse(template);
        }

        // Benchmark run
        int iterations = 100_000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            engine.parse(template);
        }

        long totalTimeNanos = System.nanoTime() - startTime;
        double totalTimeMillis = totalTimeNanos / 1_000_000.0;

        // Assert 100k parses execute in under 500ms (Adjust threshold based on target hardware)
        assertTrue(totalTimeMillis < 500.0,
                String.format("Parsing took too long: %.2f ms for %d runs", totalTimeMillis, iterations));
        System.out.printf("Parsing took %.2f for %d runs%n", totalTimeMillis, iterations);
    }
}
