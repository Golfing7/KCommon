package com.golfing8.kcommon.config.commented;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
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
