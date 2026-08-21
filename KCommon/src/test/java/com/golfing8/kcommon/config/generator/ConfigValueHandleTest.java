package com.golfing8.kcommon.config.generator;

import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigValueHandleTest {

    static class Holder {
        int myField = 5;
    }

    @Nested
    @DisplayName("get / set")
    class GetSet {
        @Test
        @DisplayName("get() reads the underlying field, set() writes it")
        void testGetSet() {
            Holder holder = new Holder();
            FieldHandle<Integer> fieldHandle = new FieldHandle<>("myField", Holder.class);
            ConfigValueHandle handle = new ConfigValueHandle(fieldHandle, null, holder);

            assertEquals(5, handle.get());
            handle.set(10);
            assertEquals(10, holder.myField);
            assertEquals(10, handle.get());
        }
    }

    @Nested
    @DisplayName("getFormattedPath")
    class FormattedPath {
        @Test
        @DisplayName("Without an annotation, converts the camelCase field name to kebab-case")
        void testFormattedPathWithoutAnnotationUsesKebabCase() {
            Holder holder = new Holder();
            FieldHandle<Integer> fieldHandle = new FieldHandle<>("myField", Holder.class);
            ConfigValueHandle handle = new ConfigValueHandle(fieldHandle, null, holder);

            assertEquals("my-field", handle.getFormattedPath(""));
            assertEquals("parent.my-field", handle.getFormattedPath("parent"));
        }

        @Test
        @DisplayName("An annotation's label overrides the derived kebab-case name")
        void testFormattedPathUsesAnnotationLabel() throws NoSuchFieldException {
            Holder holder = new Holder();
            FieldHandle<Integer> fieldHandle = new FieldHandle<>("myField", Holder.class);
            Conf annotation = AnnotatedHolder.class.getDeclaredField("labelled").getAnnotation(Conf.class);

            ConfigValueHandle handle = new ConfigValueHandle(fieldHandle, annotation, holder);
            assertEquals("custom-label", handle.getFormattedPath(""));
        }

        class AnnotatedHolder {
            @Conf(label = "custom-label")
            int labelled;
        }
    }

    @Nested
    @DisplayName("load")
    class Load {
        @Test
        @DisplayName("Writes the field's current value into the config when the path is absent")
        void testLoadWritesDefaultWhenAbsent() {
            Configuration configuration = new Configuration(Paths.get("ConfigValueHandleTest_write.yml"));
            Holder holder = new Holder();
            FieldHandle<Integer> fieldHandle = new FieldHandle<>("myField", Holder.class);
            ConfigValueHandle handle = new ConfigValueHandle(fieldHandle, null, holder);

            boolean modified = handle.load(configuration, "my-field", false, false);
            assertTrue(modified);
            assertEquals(5, configuration.getInt("my-field"));
        }

        @Test
        @DisplayName("readOnly=true skips writing the default value when the path is absent")
        void testLoadReadOnlySkipsWrite() {
            Configuration configuration = new Configuration(Paths.get("ConfigValueHandleTest_readonly.yml"));
            Holder holder = new Holder();
            FieldHandle<Integer> fieldHandle = new FieldHandle<>("myField", Holder.class);
            ConfigValueHandle handle = new ConfigValueHandle(fieldHandle, null, holder);

            boolean modified = handle.load(configuration, "my-field", true, false);
            assertFalse(modified);
            assertFalse(configuration.contains("my-field"));
        }

        @Test
        @DisplayName("Reads an existing config value into the field")
        void testLoadReadsExistingValueIntoField() {
            Configuration configuration = new Configuration(Paths.get("ConfigValueHandleTest_read.yml"));
            configuration.loadFromString("my-field: 42");

            Holder holder = new Holder();
            FieldHandle<Integer> fieldHandle = new FieldHandle<>("myField", Holder.class);
            ConfigValueHandle handle = new ConfigValueHandle(fieldHandle, null, holder);

            boolean modified = handle.load(configuration, "my-field", false, false);
            assertFalse(modified);
            assertEquals(42, holder.myField);
        }
    }
}
