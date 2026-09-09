package com.golfing8.kcommon.util;

import com.golfing8.kcommon.struct.placeholder.Placeholder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Covers {@link Placeholders}, the lightweight placeholder parser used outside of the
 * MiniMessage/Component pipeline (e.g. for item lore/names).
 */
class PlaceholdersTest {

    @Nested
    @DisplayName("parseFully")
    class ParseFully {
        @Test
        @DisplayName("No placeholders returns the message unchanged")
        void testNoPlaceholders() {
            assertEquals("Hello World", Placeholders.parseFully("Hello World"));
        }

        @Test
        @DisplayName("Key/value pairs are substituted using {KEY} bracket convention")
        void testKeyValuePairs() {
            assertEquals("Hello World", Placeholders.parseFully("Hello {NAME}", "NAME", "World"));
        }

        @Test
        @DisplayName("Multiple key/value pairs are all substituted")
        void testMultipleKeyValuePairs() {
            assertEquals("A=1, B=2", Placeholders.parseFully("A={A}, B={B}", "A", 1, "B", 2));
        }

        @Test
        @DisplayName("Placeholder objects use their own label (not auto-bracketed) and value")
        void testPlaceholderObjectInput() {
            Placeholder placeholder = Placeholder.curlyTrustedArg("NAME", "World", true);
            assertEquals("Hello World", Placeholders.parseFully("Hello {NAME}", placeholder));
        }

        @Test
        @DisplayName("Placeholder objects and key/value pairs can be mixed")
        void testMixedPlaceholderAndKeyValue() {
            Placeholder placeholder = Placeholder.curlyTrustedArg("NAME", "World", true);
            assertEquals("Hello World, count=5", Placeholders.parseFully("Hello {NAME}, count={COUNT}", placeholder, "COUNT", 5));
        }

        @Test
        @DisplayName("A null value is rendered as the literal string \"null\"")
        void testNullValueBecomesLiteralNull() {
            assertEquals("Value: null", Placeholders.parseFully("Value: {V}", "V", null));
        }

        @Test
        @DisplayName("A trailing key with no matching value throws")
        void testUnbalancedThrows() {
            assertThrows(IllegalArgumentException.class, () -> Placeholders.parseFully("Hello {NAME}", "NAME"));
        }
    }

    @Nested
    @DisplayName("start (StringPlaceholders builder)")
    class StringPlaceholdersBuilder {
        @Test
        @DisplayName("keys/values are substituted in matching order")
        void testKeysValuesGet() {
            String result = Placeholders.start("A={A} B={B}")
                    .keys("{A}", "{B}")
                    .values("1", "2")
                    .get();
            assertEquals("A=1 B=2", result);
        }

        @Test
        @DisplayName("Extra keys beyond the provided values are left unsubstituted")
        void testMoreKeysThanValues() {
            String result = Placeholders.start("A={A} B={B}")
                    .keys("{A}", "{B}")
                    .values("1")
                    .get();
            assertEquals("A=1 B={B}", result);
        }
    }
}
