package com.golfing8.kcommon.config.commented;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Nested
    @DisplayName("load/save round trip")
    class RoundTrip {
        @Test
        @DisplayName("Saving and reloading preserves values and comments")
        void testSaveAndLoadPreservesValuesAndComments(@TempDir Path tempDir) {
            Path file = tempDir.resolve("config.yml");
            Configuration configuration = new Configuration(file);
            configuration.set("key", "value", "A comment");
            configuration.set("nested.number", 5);
            configuration.save();

            Configuration reloaded = new Configuration(file);
            reloaded.load();
            assertEquals("value", reloaded.getString("key"));
            assertEquals(5, reloaded.getInt("nested.number"));
            assertArrayEquals(new String[]{"# A comment"}, reloaded.getComments().get("key"));
        }

        @Test
        @DisplayName("save() clears the modified flag")
        void testSaveClearsModifiedFlag(@TempDir Path tempDir) {
            Configuration configuration = new Configuration(tempDir.resolve("config.yml"));
            configuration.set("key", "value");
            assertTrue(configuration.isModified());

            configuration.save();
            assertFalse(configuration.isModified());
        }
    }

    @Nested
    @DisplayName("loadFromString")
    class LoadFromString {
        @Test
        @DisplayName("Populates values and per-key comments from a YAML string")
        void testLoadFromString() {
            Configuration configuration = new Configuration(Paths.get("ConfigurationTest_loadFromString.yml"));
            configuration.loadFromString(
                    "# Header\n" +
                            "key: value\n" +
                            "# Nested comment\n" +
                            "nested:\n" +
                            "  child: 1"
            );

            assertEquals("value", configuration.getString("key"));
            assertEquals(1, configuration.getInt("nested.child"));
            assertArrayEquals(new String[]{"# Header"}, configuration.getComments().get("key"));
        }
    }

    @Nested
    @DisplayName("contains / set / get")
    class ContainsSetGet {
        @Test
        @DisplayName("contains() reflects presence of a key after set()")
        void testContainsReflectsSet() {
            Configuration configuration = new Configuration(Paths.get("ConfigurationTest_contains.yml"));
            assertFalse(configuration.contains("key"));
            configuration.set("key", "value");
            assertTrue(configuration.contains("key"));
        }

        @Test
        @DisplayName("get() wraps nested sections in a WrappedConfigurationSection")
        void testGetWrapsNestedSections() {
            Configuration configuration = new Configuration(Paths.get("ConfigurationTest_wrap.yml"));
            configuration.loadFromString("nested:\n  child: 1");

            Object value = configuration.get("nested");
            assertTrue(value instanceof WrappedConfigurationSection);
        }

        @Test
        @DisplayName("getStringList reads a YAML list")
        void testGetStringList() {
            Configuration configuration = new Configuration(Paths.get("ConfigurationTest_list.yml"));
            configuration.loadFromString("items:\n  - a\n  - b\n  - c");

            assertEquals(Arrays.asList("a", "b", "c"), configuration.getStringList("items"));
        }
    }

    @Nested
    @DisplayName("getFileName / getFileNameNoExtension")
    class FileNaming {
        @Test
        @DisplayName("Derives file name and extension-stripped name from the config path")
        void testFileNaming(@TempDir Path tempDir) {
            Configuration configuration = new Configuration(tempDir.resolve("my-config.yml"));
            assertEquals("my-config.yml", configuration.getFileName());
            assertEquals("my-config", configuration.getFileNameNoExtension());
        }
    }

    @Nested
    @DisplayName("setComments")
    class SetComments {
        @Test
        @DisplayName("Passing no comments clears any existing comments for the path")
        void testSetCommentsEmptyClears() {
            Configuration configuration = new Configuration(Paths.get("ConfigurationTest_clearComments.yml"));
            configuration.set("key", "value", "Comment");
            assertNotNull(configuration.getComments().get("key"));

            configuration.setComments("key");
            assertNull(configuration.getComments().get("key"));
        }
    }
}
