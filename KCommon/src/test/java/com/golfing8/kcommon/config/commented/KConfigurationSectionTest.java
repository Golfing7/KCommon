package com.golfing8.kcommon.config.commented;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class KConfigurationSectionTest {

    @Nested
    @DisplayName("forEachKey")
    class ForEachKey {
        @Test
        @DisplayName("Visits each direct key under the given path")
        void testVisitsDirectKeysUnderPath() {
            Configuration configuration = new Configuration(Paths.get("KConfigurationSectionTest_forEachKey.yml"));
            configuration.loadFromString("section:\n  a: 1\n  b: 2\n  c: 3");

            List<String> visited = new ArrayList<>();
            configuration.forEachKey("section", visited::add);

            assertEquals(3, visited.size());
            assertTrue(visited.containsAll(java.util.Arrays.asList("section.a", "section.b", "section.c")));
        }

        @Test
        @DisplayName("Does nothing when the path doesn't resolve to a section")
        void testMissingSectionIsNoOp() {
            Configuration configuration = new Configuration(Paths.get("KConfigurationSectionTest_forEachKeyMissing.yml"));
            configuration.loadFromString("other: 1");

            List<String> visited = new ArrayList<>();
            configuration.forEachKey("missing", visited::add);
            assertEquals(0, visited.size());
        }

        @Test
        @DisplayName("Visits each direct key at the root when called with no path")
        void testVisitsDirectKeysAtRoot() {
            Configuration configuration = new Configuration(Paths.get("KConfigurationSectionTest_forEachKeyRoot.yml"));
            configuration.loadFromString("a: 1\nb: 2");

            List<String> visited = new ArrayList<>();
            configuration.forEachKey(visited::add);

            assertEquals(2, visited.size());
            assertTrue(visited.containsAll(java.util.Arrays.asList("a", "b")));
        }

        @Test
        @DisplayName("Does nothing on a nested section when the sub-path doesn't resolve to a section")
        void testMissingSectionIsNoOpOnNestedSection() {
            Configuration configuration = new Configuration(Paths.get("KConfigurationSectionTest_forEachKeyNestedMissing.yml"));
            configuration.loadFromString("section:\n  a: 1");

            WrappedConfigurationSection section = configuration.getConfigurationSection("section");
            List<String> visited = new ArrayList<>();
            section.forEachKey("missing", visited::add);
            assertEquals(0, visited.size());
        }
    }

    @Nested
    @DisplayName("forEachSubsection")
    class ForEachSubsection {
        @Test
        @DisplayName("Visits each direct subsection under the given path")
        void testVisitsDirectSubsections() {
            Configuration configuration = new Configuration(Paths.get("KConfigurationSectionTest_forEachSubsection.yml"));
            configuration.loadFromString("section:\n  sub1:\n    x: 1\n  sub2:\n    y: 2\n  plain: 3");

            List<String> visitedNames = new ArrayList<>();
            configuration.forEachSubsection("section", section -> visitedNames.add(section.getName()));

            assertEquals(2, visitedNames.size());
            assertTrue(visitedNames.containsAll(java.util.Arrays.asList("sub1", "sub2")));
        }
    }

    @Nested
    @DisplayName("get(path, Class)")
    class Get {
        @Test
        @DisplayName("Returns an empty Optional when the path doesn't exist")
        void testEmptyWhenMissing() {
            Configuration configuration = new Configuration(Paths.get("KConfigurationSectionTest_getMissing.yml"));
            assertEquals(Optional.empty(), configuration.get("missing", Integer.class));
        }

        @Test
        @DisplayName("Returns the resolved value when the path exists")
        void testPresentWhenExists() {
            Configuration configuration = new Configuration(Paths.get("KConfigurationSectionTest_getPresent.yml"));
            configuration.loadFromString("key: 42");

            assertEquals(Optional.of(42), configuration.get("key", Integer.class));
        }
    }

    @Nested
    @DisplayName("getOrLoad")
    class GetOrLoad {
        @Test
        @DisplayName("Loads the value from the source config when absent locally, and persists it")
        void testLoadsFromSourceWhenAbsent(@TempDir Path tempDir) {
            Configuration source = new Configuration(Paths.get("KConfigurationSectionTest_source.yml"));
            source.loadFromString("key: 7");

            Configuration configuration = new Configuration(tempDir.resolve("getOrLoad.yml"));
            configuration.setSource(source);

            Optional<Integer> value = configuration.getOrLoad("key", Integer.class);
            assertEquals(Optional.of(7), value);
            assertTrue(configuration.contains("key"));
        }

        @Test
        @DisplayName("Returns empty when absent both locally and in the source")
        void testEmptyWhenAbsentEverywhere() {
            Configuration configuration = new Configuration(Paths.get("KConfigurationSectionTest_getOrLoadEmpty.yml"));
            assertEquals(Optional.empty(), configuration.getOrLoad("key", Integer.class));
        }
    }
}
