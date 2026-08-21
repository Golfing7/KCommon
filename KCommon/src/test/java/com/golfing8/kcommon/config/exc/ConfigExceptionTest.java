package com.golfing8.kcommon.config.exc;

import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.commented.MConfiguration;
import com.golfing8.kcommon.module.Module;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigExceptionTest {

    @Nested
    @DisplayName("Message prefix formatting")
    class Prefix {
        @Test
        @DisplayName("No section produces no prefix")
        void testNullSectionNoPrefix() {
            ConfigException exc = new ConfigException(null, "Something broke");
            assertEquals("Something broke", exc.getMessage());
        }

        @Test
        @DisplayName("A plain ConfigurationSection is prefixed with its current path")
        void testPlainSectionPrefixedWithCurrentPath() {
            YamlConfiguration root = new YamlConfiguration();
            root.createSection("nested");
            ConfigurationSection nested = root.getConfigurationSection("nested");

            ConfigException exc = new ConfigException(nested, "Something broke");
            assertEquals(nested.getCurrentPath() + "Something broke", exc.getMessage());
        }

        @Test
        @DisplayName("A Configuration is prefixed with its name")
        void testConfigurationPrefixedWithName() {
            Configuration configuration = new Configuration(Paths.get("ConfigExceptionTest.yml"));
            ConfigException exc = new ConfigException(configuration, "Something broke");
            assertEquals(configuration.getName() + ": Something broke", exc.getMessage());
        }

        @Test
        @DisplayName("An MConfiguration is prefixed with module name and config name")
        void testMConfigurationPrefixedWithModuleAndName(@TempDir Path tempDir) {
            Module module = mock(Module.class);
            when(module.getModuleName()).thenReturn("my-module");

            MConfiguration configuration = new MConfiguration(tempDir.resolve("config.yml"), module);
            ConfigException exc = new ConfigException(configuration, "Something broke");
            assertEquals("my-module_" + configuration.getName() + ": Something broke", exc.getMessage());
        }

        @Test
        @DisplayName("The cause-accepting constructor preserves both prefix and cause")
        void testCauseConstructorPreservesCause() {
            RuntimeException cause = new RuntimeException("root cause");
            ConfigException exc = new ConfigException(null, "Something broke", cause);
            assertEquals("Something broke", exc.getMessage());
            assertSame(cause, exc.getCause());
        }
    }
}
