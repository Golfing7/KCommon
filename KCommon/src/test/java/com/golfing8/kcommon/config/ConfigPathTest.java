package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.commented.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigPathTest {

    @Nested
    @DisplayName("parse")
    class Parse {
        @Test
        @DisplayName("A single segment parses as a bare path")
        void testSingleSegment() {
            ConfigPath path = ConfigPath.parse("some.path");
            assertEquals("some.path", path.toString());
        }

        @Test
        @DisplayName("Two segments parse as config:path")
        void testTwoSegments() {
            ConfigPath path = ConfigPath.parse("myconfig:some.path");
            assertEquals("myconfig:some.path", path.toString());
        }

        @Test
        @DisplayName("Three segments parse as module:config:path")
        void testThreeSegments() {
            ConfigPath path = ConfigPath.parse("mymodule:myconfig:some.path");
            assertEquals("mymodule:myconfig:some.path", path.toString());
        }

        @Test
        @DisplayName("Semicolons are also accepted as separators")
        void testSemicolonSeparator() {
            ConfigPath path = ConfigPath.parse("mymodule;myconfig;some.path");
            assertEquals("mymodule:myconfig:some.path", path.toString());
        }
    }

    @Nested
    @DisplayName("enumerate(ConfigurationSection)")
    class EnumerateSection {
        @Test
        @DisplayName("Returns a single entry when the path exists in the section")
        void testEnumeratesExistingPath() {
            Configuration configuration = new Configuration(Paths.get("ConfigPathTest_exists.yml"));
            configuration.loadFromString("some:\n  path: 5");

            ConfigPath path = ConfigPath.parse("some.path");
            List<ConfigEntry> entries = path.enumerate(configuration);
            assertEquals(1, entries.size());
            assertEquals(5, entries.get(0).get());
        }

        @Test
        @DisplayName("Returns an empty list when the path does not exist in the section")
        void testEnumerateMissingPathIsEmpty() {
            Configuration configuration = new Configuration(Paths.get("ConfigPathTest_missing.yml"));
            configuration.loadFromString("other: 1");

            ConfigPath path = ConfigPath.parse("some.path");
            assertTrue(path.enumerate(configuration).isEmpty());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {
        @Test
        @DisplayName("Omits module/config segments that weren't specified")
        void testToStringOmitsUnspecifiedSegments() {
            assertEquals("path", new ConfigPath(null, null, "path").toString());
            assertEquals("conf:path", new ConfigPath(null, "conf", "path").toString());
            assertEquals("mod:conf:path", new ConfigPath("mod", "conf", "path").toString());
        }
    }
}
