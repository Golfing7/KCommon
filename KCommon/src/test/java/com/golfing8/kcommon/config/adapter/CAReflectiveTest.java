package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.commented.Configuration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Additional coverage for {@link CAReflective} beyond what
 * {@code com.golfing8.kcommon.module.test.config.CASerializableTest} already exercises
 * (simple field mapping, config-delegated values, and polymorphic type resolution).
 * This focuses on the "flatten" option and "_key" field population, which aren't covered there.
 */
class CAReflectiveTest {

    @CASerializable.Options(flatten = true)
    public static class FlattenedSingleField implements CASerializable {
        public int amount = 0;
    }

    public static class KeyedObject implements CASerializable {
        public String _key;
        public int value;
    }

    @Test
    @DisplayName("Flattens a single-field serializable into a bare primitive instead of a map")
    void testFlattenSerializesToBarePrimitive() {
        FlattenedSingleField object = new FlattenedSingleField();
        object.amount = 42;

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(object);
        assertEquals(42, primitive.getPrimitive());
    }

    @Test
    @DisplayName("Deserializes a flattened bare primitive back into the single field")
    void testFlattenDeserializeFromBarePrimitive() {
        ConfigPrimitive primitive = ConfigPrimitive.ofInt(99);
        FlattenedSingleField result = ConfigTypeRegistry.getFromType(primitive, FlattenedSingleField.class);
        assertEquals(99, result.amount);
    }

    @Test
    @DisplayName("Populates the '_key' field with the config section's own name")
    void testKeyFieldPopulatedFromConfigSectionName(@TempDir Path tempDir) {
        String yaml =
                "my-object:\n" +
                        "  value: 7\n";

        Configuration configuration = new Configuration(tempDir.resolve("test.yml"));
        configuration.loadFromString(yaml);

        KeyedObject result = ConfigTypeRegistry.getFromType(new ConfigEntry(configuration, "my-object"), KeyedObject.class);
        assertEquals("my-object", result._key);
        assertEquals(7, result.value);
    }
}
