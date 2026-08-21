package com.golfing8.kcommon.config.generator;

import com.golfing8.kcommon.config.commented.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConfigClassTest {

    static class SimpleConfig extends ConfigClass {
        @Conf("A number")
        public int number = 5;
        public String unannotatedButStillLoaded = "default";

        SimpleConfig() {
            super();
        }
    }

    @Nested
    @DisplayName("Basic field loading")
    class BasicLoading {
        @Test
        @DisplayName("initConfig + loadValues writes default field values into an empty config")
        void testWritesDefaultsIntoEmptyConfig() {
            SimpleConfig config = new SimpleConfig();
            config.initConfig();

            Configuration configuration = new Configuration(Paths.get("ConfigClassTest_defaults.yml"));
            boolean modified = config.loadValues(configuration);

            assertTrue(modified);
            assertEquals(5, configuration.getInt("number"));
            assertEquals("default", configuration.getString("unannotated-but-still-loaded"));
        }

        @Test
        @DisplayName("loadValues reads existing config values back into the fields")
        void testReadsExistingValuesIntoFields() {
            Configuration configuration = new Configuration(Paths.get("ConfigClassTest_read.yml"));
            configuration.loadFromString("number: 99\nunannotated-but-still-loaded: hello");

            SimpleConfig config = new SimpleConfig();
            config.initConfig();
            boolean modified = config.loadValues(configuration);

            assertFalse(modified);
            assertEquals(99, config.number);
            assertEquals("hello", config.unannotatedButStillLoaded);
        }
    }

    static class ParentConfig extends ConfigClass {
        @Conf("parent value")
        public int parentValue = 1;

        public static class ChildConfig extends ConfigClass {
            @Conf("child value")
            public int childValue = 2;
        }
    }

    @Nested
    @DisplayName("Nested config classes")
    class NestedChildren {
        @Test
        @DisplayName("Declared inner ConfigClass classes are auto-resolved as children and namespaced under their simple name")
        void testResolvesChildrenAndNamespacesPath() {
            ParentConfig parent = new ParentConfig();
            parent.initConfig();

            Configuration configuration = new Configuration(Paths.get("ConfigClassTest_nested.yml"));
            parent.loadValues(configuration);

            assertEquals(1, configuration.getInt("parent-value"));
            assertEquals(2, configuration.getInt("ChildConfig.child-value"));

            ParentConfig.ChildConfig child = parent.getChild(ParentConfig.ChildConfig.class);
            assertEquals(2, child.childValue);
        }

        @Test
        @DisplayName("getChild throws for a class that isn't a registered child")
        void testGetChildThrowsForUnknownClass() {
            ParentConfig parent = new ParentConfig();
            parent.initConfig();

            assertThrows(NoSuchElementException.class, () -> parent.getChild(SimpleConfig.class));
        }
    }

    static class RequireAnnotationConfig extends ConfigClass {
        @Conf("Annotated")
        public int annotated = 1;
        public int notAnnotated = 2;
    }

    @Nested
    @DisplayName("requireAnnotation")
    class RequireAnnotation {
        @Test
        @DisplayName("When enabled, only @Conf-annotated fields are loaded")
        void testOnlyAnnotatedFieldsLoadedWhenRequired() {
            RequireAnnotationConfig config = new RequireAnnotationConfig();
            config.setRequireAnnotation(true);
            config.initConfig();

            Configuration configuration = new Configuration(Paths.get("ConfigClassTest_requireAnnotation.yml"));
            config.loadValues(configuration);

            assertTrue(configuration.contains("annotated"));
            assertFalse(configuration.contains("not-annotated"));
        }
    }

    static class SourcedConfig extends ConfigClass {
        SourcedConfig() {
            super();
        }
    }

    static class ExtraSource implements ConfigClassSource {
        @Conf("From a source")
        public int sourceValue = 42;
    }

    @Nested
    @DisplayName("addSource")
    class AddSource {
        @Test
        @DisplayName("Fields declared on an added ConfigClassSource are loaded alongside the class's own fields")
        void testSourceFieldsAreLoaded() {
            SourcedConfig config = new SourcedConfig();
            config.addSource(ExtraSource.class);
            config.initConfig();

            Configuration configuration = new Configuration(Paths.get("ConfigClassTest_source.yml"));
            config.loadValues(configuration);

            assertEquals(42, configuration.getInt("source-value"));
        }
    }

    @Nested
    @DisplayName("getConfigNames")
    class ConfigNames {
        @Test
        @DisplayName("Defaults to {'config'} when no @Conf annotation declares a custom config file")
        void testDefaultsToConfig() {
            SimpleConfig config = new SimpleConfig();
            config.initConfig();

            Set<String> names = config.getConfigNames();
            assertEquals(Collections.singleton("config"), names);
        }

        class CustomConfigNameConfig extends ConfigClass {
            @Conf(value = "In a custom file", config = "custom")
            public int value = 1;
        }

        @Test
        @DisplayName("Uses the lowercase config() value from the @Conf annotation when present")
        void testUsesCustomConfigName() {
            CustomConfigNameConfig config = new CustomConfigNameConfig();
            config.initConfig();

            assertEquals(Collections.singleton("custom"), config.getConfigNames());
        }
    }
}
