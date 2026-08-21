package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.adapter.ConfigAdapter;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.module.test.util.FakeServer;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigTypeRegistryTest {

    @BeforeAll
    static void bootstrap() {
        // Several registered adapters touch Bukkit classes at static-init time.
        FakeServer.getServer();
    }

    @Nested
    @DisplayName("Plain (unadapted) types")
    class PlainTypes {
        @Test
        @DisplayName("getFromType falls back to the raw section value when no adapter is registered for the type")
        void testFallsBackToRawValueForUnadaptedType() {
            Configuration configuration = new Configuration(Paths.get("ConfigTypeRegistryTest_plain.yml"));
            configuration.loadFromString("key: 42");

            Integer value = ConfigTypeRegistry.getFromType(new ConfigEntry(configuration, "key"), Integer.class);
            assertEquals(42, value);
        }

        @Test
        @DisplayName("toPrimitive returns the value unchanged for a type with no adapter")
        void testToPrimitiveNoAdapter() {
            ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive("hello");
            assertEquals("hello", primitive.getPrimitive());
        }

        @Test
        @DisplayName("toPrimitive returns a null primitive for a null value")
        void testToPrimitiveNull() {
            ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(null);
            assertNull(primitive.getPrimitive());
        }
    }

    @Nested
    @DisplayName("findAdapter")
    class FindAdapter {
        @Test
        @DisplayName("Returns null for Object.class (base case)")
        void testObjectClassHasNoAdapter() {
            assertNull(ConfigTypeRegistry.findAdapter(Object.class));
        }

        @Test
        @DisplayName("Walks up to a superclass adapter when no exact match is registered (CAEnum adapts any Enum subtype)")
        void testFindsAdapterViaSuperclassWalk() {
            assertNotNull(ConfigTypeRegistry.findAdapter(java.time.DayOfWeek.class));
        }

        @Test
        @DisplayName("A newly registered adapter is found for its exact declared type, and clears prior lookup cache")
        void testRegisterAdapterIsFoundAndClearsCache() {
            class Marker {
            }

            ConfigAdapter<Marker> adapter = new ConfigAdapter<Marker>() {
                @Override
                public Class<Marker> getAdaptType() {
                    return Marker.class;
                }

                @Override
                public ConfigPrimitive toPrimitive(Marker marker) {
                    return ConfigPrimitive.of("marker");
                }

                @Override
                public Marker toPOJO(ConfigPrimitive primitive, FieldType type) {
                    return new Marker();
                }
            };

            assertNull(ConfigTypeRegistry.findAdapter(Marker.class));
            ConfigTypeRegistry.registerAdapter(adapter);
            assertEquals(adapter, ConfigTypeRegistry.findAdapter(Marker.class));
        }
    }

    @Nested
    @DisplayName("setInConfig")
    class SetInConfig {
        @Test
        @DisplayName("Sets a plain value converted through toPrimitive")
        void testSetsPlainValue() {
            YamlConfiguration section = new YamlConfiguration();
            ConfigTypeRegistry.setInConfig(section, "key", 5);
            assertEquals(5, section.getInt("key"));
        }

        @Test
        @DisplayName("Sets a ConfigurationSection value directly, bypassing primitive conversion")
        void testSetsConfigurationSectionDirectly() {
            YamlConfiguration section = new YamlConfiguration();
            YamlConfiguration nested = new YamlConfiguration();
            nested.set("inner", 1);

            ConfigTypeRegistry.setInConfig(section, "key", nested);
            assertEquals(1, section.getConfigurationSection("key").getInt("inner"));
        }
    }

    // NOTE: "!delegate!" resolution (ConfigTypeRegistry#loadFromDelegate) resolves its path via
    // ConfigPath#enumerate(), which walks the global Modules registry (com.golfing8.kcommon.module.Modules).
    // That registry isn't bootstrappable in this unit test harness (no live plugin/module lifecycle),
    // so delegate-prefixed value resolution isn't covered here.
}
