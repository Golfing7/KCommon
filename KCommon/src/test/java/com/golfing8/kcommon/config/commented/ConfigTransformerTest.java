package com.golfing8.kcommon.config.commented;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigTransformerTest {

    @Test
    @DisplayName("Iterates nested and quoted YAML keys in file order")
    void iteratesNestedAndQuotedKeysInOrder() {
        String yaml =
                "# Header comment\n" +
                        "root:\n" +
                        "  child: 1\n" +
                        "  'quoted-key': 2\n" +
                        "sibling:\n" +
                        "  nested:\n" +
                        "    leaf: 3";

        ConfigTransformer transformer = new ConfigTransformer(yaml);

        assertIterableEquals(
                Arrays.asList("root", "root.child", "root.quoted-key", "sibling", "sibling.nested", "sibling.nested.leaf"),
                collectKeys(transformer)
        );
    }

    @Test
    @DisplayName("Exposes the preceding comments for each parsed key")
    void exposesThePrecedingCommentsForEachParsedKey() {
        String yaml =
                "# Root comment\n" +
                        "root:\n" +
                        "  # Child comment\n" +
                        "  child: 1\n" +
                        "\n" +
                        "# Sibling comment\n" +
                        "sibling: 2";

        ConfigTransformer transformer = new ConfigTransformer(yaml);

        assertEquals("root", transformer.next());
        assertEquals(Collections.singletonList("# Root comment"), commentsOnly(transformer.getJunk()));

        assertEquals("root.child", transformer.next());
        assertEquals(Collections.singletonList("# Child comment"), commentsOnly(transformer.getJunk()));

        assertEquals("sibling", transformer.next());
        assertEquals(Collections.singletonList("# Sibling comment"), commentsOnly(transformer.getJunk()));
    }

    @Test
    @DisplayName("Inserts missing comments before the current key with matching indent")
    void insertsMissingCommentsBeforeTheCurrentKeyWithMatchingIndent() {
        String yaml =
                "root:\n" +
                        "  child: 1\n" +
                        "sibling: 2";

        ConfigTransformer transformer = new ConfigTransformer(yaml);

        for (String key : transformer) {
            if ("root.child".equals(key)) {
                transformer.insertComment("# Child comment");
            }
        }

        assertEquals(
                Arrays.asList(
                        "root:",
                        "  # Child comment",
                        "  child: 1",
                        "sibling: 2"
                ),
                transformer.getTransformedLines()
        );
    }

    @Test
    @DisplayName("Skips inserting duplicate comments when a key already has one")
    void skipsInsertingDuplicateCommentsWhenAKeyAlreadyHasOne() {
        String yaml =
                "root:\n" +
                        "  # Existing comment\n" +
                        "  child: 1";

        ConfigTransformer transformer = new ConfigTransformer(yaml);

        for (String key : transformer) {
            if ("root.child".equals(key)) {
                transformer.insertComment("# New comment");
            }
        }

        assertEquals(
                Arrays.asList(
                        "root:",
                        "  # Existing comment",
                        "  child: 1"
                ),
                transformer.getTransformedLines()
        );
    }

    @Test
    @DisplayName("Round-trips loaded comments through configuration saves")
    void roundTripsLoadedCommentsThroughConfigurationSaves() {
        String yaml =
                "# Root comment\n" +
                        "root:\n" +
                        "  # Child comment\n" +
                        "  child: 1\n" +
                        "sibling: 2";

        Configuration configuration = new Configuration(Paths.get("config-transformer-test.yml"));
        configuration.loadFromString(yaml);

        assertArrayEquals(new String[]{"# Root comment"}, configuration.getComments().get("root"));
        assertArrayEquals(new String[]{"# Child comment"}, configuration.getComments().get("root.child"));

        configuration.set("sibling", 2, "Sibling comment");

        String saved = configuration.saveToString();
        assertTrue(saved.contains("# Root comment\nroot:"));
        assertTrue(saved.contains("  # Child comment\n  child: 1"));
        assertTrue(saved.contains("# Sibling comment\nsibling: 2"));
    }

    @Test
    @DisplayName("Tracks key depth for indentation wider than two spaces")
    void tracksKeyDepthForWideIndentation() {
        String yaml =
                "root:\n" +
                        "    a: 1\n" +
                        "    b:\n" +
                        "        c: 2\n" +
                        "    d: 3\n" +
                        "sibling: 4";

        assertIterableEquals(
                Arrays.asList("root", "root.a", "root.b", "root.b.c", "root.d", "sibling"),
                collectKeys(new ConfigTransformer(yaml))
        );
    }

    @Test
    @DisplayName("Recognizes double-quoted keys and keys starting with symbols")
    void recognizesDoubleQuotedAndSymbolKeys() {
        String yaml =
                "first: 1\n" +
                        "\"double-quoted\": 2\n" +
                        "_underscore:\n" +
                        "  child: 3";

        assertIterableEquals(
                Arrays.asList("first", "double-quoted", "_underscore", "_underscore.child"),
                collectKeys(new ConfigTransformer(yaml))
        );
    }

    @Test
    @DisplayName("Parses single-quoted keys containing escaped quotes and colons")
    void parsesSingleQuotedKeysWithEscapesAndColons() {
        String yaml =
                "'it''s': 1\n" +
                        "'a:b': 2\n" +
                        "'key': 'value with '' quote'";

        assertIterableEquals(
                Arrays.asList("it's", "a:b", "key"),
                collectKeys(new ConfigTransformer(yaml))
        );
    }

    @Test
    @DisplayName("Handles configs with many keys without overflowing the stack")
    void handlesManyKeysWithoutOverflow() {
        int keyCount = 50_000;
        StringBuilder yaml = new StringBuilder();
        for (int i = 0; i < keyCount; i++) {
            yaml.append("key").append(i).append(": ").append(i).append('\n');
        }

        assertTimeoutPreemptively(Duration.ofSeconds(2), () ->
                assertEquals(keyCount, collectKeys(new ConfigTransformer(yaml.toString())).size())
        );
    }

    @Test
    @DisplayName("Throws NoSuchElementException when iterated past the end")
    void throwsNoSuchElementWhenExhausted() {
        ConfigTransformer transformer = new ConfigTransformer("only: 1");

        assertEquals("only", transformer.next());
        assertThrows(NoSuchElementException.class, transformer::next);
    }

    @Test
    @DisplayName("Ignores colons inside block scalar content")
    void ignoresColonsInsideBlockScalars() {
        String yaml =
                "description: |\n" +
                        "  Some text: with a colon\n" +
                        "next: 1";

        assertIterableEquals(
                Arrays.asList("description", "next"),
                collectKeys(new ConfigTransformer(yaml))
        );
    }

    @Test
    @DisplayName("Does not treat '#' lines inside block scalars as comments")
    void doesNotTreatBlockScalarHashLinesAsComments() {
        String yaml =
                "description: |\n" +
                        "  # heading inside the text\n" +
                        "next: 1";

        ConfigTransformer transformer = new ConfigTransformer(yaml);
        for (String key : transformer) {
            if ("next".equals(key)) {
                transformer.insertComment("# Next comment");
            }
        }

        assertEquals(
                Arrays.asList(
                        "description: |",
                        "  # heading inside the text",
                        "# Next comment",
                        "next: 1"
                ),
                transformer.getTransformedLines()
        );
    }

    @Test
    @DisplayName("Keeps comments that follow the end of a block scalar")
    void keepsCommentsAfterBlockScalar() {
        String yaml =
                "description: |\n" +
                        "  text\n" +
                        "# Next comment\n" +
                        "next: 1";

        ConfigTransformer transformer = new ConfigTransformer(yaml);

        assertEquals("description", transformer.next());
        assertEquals("next", transformer.next());
        assertEquals(Collections.singletonList("# Next comment"), commentsOnly(transformer.getJunk()));
    }

    @Test
    @DisplayName("Recognizes every block scalar indicator variant")
    void recognizesBlockScalarIndicatorVariants() {
        for (String indicator : Arrays.asList("|-", "|+", ">", ">-", "|2", "| # note")) {
            String yaml =
                    "description: " + indicator + "\n" +
                            "  Some text: with a colon\n" +
                            "next: 1";

            assertIterableEquals(
                    Arrays.asList("description", "next"),
                    collectKeys(new ConfigTransformer(yaml)),
                    "indicator " + indicator
            );
        }
    }

    @Test
    @DisplayName("Parses quoted keys with whitespace before the colon")
    void parsesQuotedKeyWithSpaceBeforeColon() {
        String yaml =
                "'key' : 1\n" +
                        "next: 2";

        assertIterableEquals(
                Arrays.asList("key", "next"),
                collectKeys(new ConfigTransformer(yaml))
        );
    }

    @Test
    @DisplayName("Does not end a single-quoted key inside an escaped quote")
    void parsesSingleQuotedKeyWithEscapedQuoteBeforeColon() {
        assertIterableEquals(
                Collections.singletonList("it':s"),
                collectKeys(new ConfigTransformer("'it'':s': 1"))
        );
    }

    @Test
    @DisplayName("Unescapes keys according to their quote style")
    void unescapesKeysByQuoteStyle() {
        String yaml =
                "'a\"\"b': 1\n" +
                        "\"c\\\"d\": 2";

        assertIterableEquals(
                Arrays.asList("a\"\"b", "c\"d"),
                collectKeys(new ConfigTransformer(yaml))
        );
    }

    @Test
    @DisplayName("Keeps colons that are not followed by a space inside plain keys")
    void keepsColonsInsidePlainKeys() {
        assertIterableEquals(
                Collections.singletonList("a:b"),
                collectKeys(new ConfigTransformer("a:b: 1"))
        );
    }

    @Test
    @DisplayName("Ignores colons in continuation lines of wrapped plain values")
    void ignoresColonsInPlainContinuationLines() {
        String yaml =
                "key: some long text\n" +
                        "  https://example.com\n" +
                        "next: 1";

        assertIterableEquals(
                Arrays.asList("key", "next"),
                collectKeys(new ConfigTransformer(yaml))
        );
    }

    // TODO Fix later
    //    @Test
    //    @DisplayName("Parses single-quoted keys that end in an escaped quote")
    //    void parsesSingleQuotedKeyEndingInEscapedQuote() {
    //        String yaml =
    //                "'it''': 1\n" +
    //                        "next: 2";
    //
    //        assertIterableEquals(
    //                Arrays.asList("it'", "next"),
    //                collectKeys(new ConfigTransformer(yaml))
    //        );
    //    }
    //
    //    @Test
    //    @DisplayName("Parses double-quoted keys containing escaped quotes")
    //    void parsesDoubleQuotedKeysWithEscapedQuotes() {
    //        String yaml =
    //                "\"a\\\"\": 1\n" +
    //                        "\"x\\\": y\": 2";
    //
    //        assertIterableEquals(
    //                Arrays.asList("a\"", "x\": y"),
    //                collectKeys(new ConfigTransformer(yaml))
    //        );
    //    }
    //
    //    @Test
    //    @DisplayName("Detects block scalars after a quoted key with whitespace before the colon")
    //    void recognizesBlockScalarAfterQuotedKeyWithSpaceBeforeColon() {
    //        String yaml =
    //                "'description' : |-\n" +
    //                        "  Some text: with a colon\n" +
    //                        "next: 1";
    //
    //        assertIterableEquals(
    //                Arrays.asList("description", "next"),
    //                collectKeys(new ConfigTransformer(yaml))
    //        );
    //    }
    //
    //    @Test
    //    @DisplayName("Only unescapes the sequences belonging to the key's quote style")
    //    void unescapesOnlyTheMatchingQuoteStyle() {
    //        String yaml =
    //                "\"it''s\": 1\n" +
    //                        "'a\\\"b': 2";
    //
    //        assertIterableEquals(
    //                Arrays.asList("it''s", "a\\\"b"),
    //                collectKeys(new ConfigTransformer(yaml))
    //        );
    //    }
    //
    //    @Test
    //    @DisplayName("Ignores colons in continuation lines of wrapped quoted values")
    //    void ignoresColonsInWrappedQuotedValues() {
    //        // Matches what SnakeYAML 1.x writes for a long quoted value at its default 80 column width.
    //        String yaml =
    //                "message: 'Welcome to the server, please read the rules carefully before playing on\n" +
    //                        "  this server today and every other day too. Note: be nice'\n" +
    //                        "next: 1";
    //
    //        assertIterableEquals(
    //                Arrays.asList("message", "next"),
    //                collectKeys(new ConfigTransformer(yaml))
    //        );
    //    }

    private static List<String> collectKeys(ConfigTransformer transformer) {
        List<String> keys = new ArrayList<>();
        for (String key : transformer) {
            keys.add(key);
        }
        return keys;
    }

    private static List<String> commentsOnly(List<String> lines) {
        return lines.stream()
                .map(String::trim)
                .filter(line -> line.startsWith("#"))
                .collect(Collectors.toList());
    }
}
