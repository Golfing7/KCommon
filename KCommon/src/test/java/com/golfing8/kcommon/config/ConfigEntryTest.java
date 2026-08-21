package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.generator.ConfigValueHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ConfigEntryTest {

    @Nested
    @DisplayName("ConfigEntry")
    class Entry {
        @Test
        @DisplayName("get() reads the value at the key from the section")
        void testGetReadsValue() {
            Configuration configuration = new Configuration(Paths.get("ConfigEntryTest_get.yml"));
            configuration.loadFromString("key: hello");

            ConfigEntry entry = new ConfigEntry(configuration, "key");
            assertEquals("hello", entry.get());
            assertEquals(configuration, entry.getSection());
            assertEquals("key", entry.getKey());
        }

        @Test
        @DisplayName("get() returns null for a missing key")
        void testGetMissingKeyReturnsNull() {
            Configuration configuration = new Configuration(Paths.get("ConfigEntryTest_missing.yml"));
            configuration.loadFromString("other: 1");

            ConfigEntry entry = new ConfigEntry(configuration, "key");
            assertNull(entry.get());
        }

        @Test
        @DisplayName("getPrimitive() wraps the raw value as a ConfigPrimitive")
        void testGetPrimitive() {
            Configuration configuration = new Configuration(Paths.get("ConfigEntryTest_primitive.yml"));
            configuration.loadFromString("key: 5");

            ConfigEntry entry = new ConfigEntry(configuration, "key");
            ConfigPrimitive primitive = entry.getPrimitive();
            assertEquals(5, primitive.getPrimitive());
        }
    }

    @Nested
    @DisplayName("MappedConfigEntry")
    class Mapped {
        class Holder {
            int value = 3;
        }

        @Test
        @DisplayName("Exposes the section, key, and handle it was constructed with")
        void testMappedConfigEntry() {
            Configuration configuration = new Configuration(Paths.get("ConfigEntryTest_mapped.yml"));
            configuration.loadFromString("key: 3");

            Holder holder = new Holder();
            FieldHandle<Integer> fieldHandle = new FieldHandle<>("value", Holder.class);
            ConfigValueHandle valueHandle = new ConfigValueHandle(fieldHandle, null, holder);

            MappedConfigEntry entry = new MappedConfigEntry(configuration, "key", valueHandle);
            assertEquals(configuration, entry.getSection());
            assertEquals("key", entry.getKey());
            assertSame(valueHandle, entry.getHandle());
            assertEquals(3, entry.get());
        }
    }
}
