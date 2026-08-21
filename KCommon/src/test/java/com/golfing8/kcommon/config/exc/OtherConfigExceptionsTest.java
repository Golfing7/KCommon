package com.golfing8.kcommon.config.exc;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtherConfigExceptionsTest {

    @Test
    @DisplayName("ImproperlyConfiguredValueException without an expected type mentions the option and path")
    void testImproperlyConfiguredNoExpecting() {
        YamlConfiguration root = new YamlConfiguration();
        root.createSection("nested");
        ConfigurationSection nested = root.getConfigurationSection("nested");

        ImproperlyConfiguredValueException exc = new ImproperlyConfiguredValueException(nested, "my-option");
        assertTrue(exc.getMessage().contains("my-option"));
        assertTrue(exc.getMessage().contains(nested.getCurrentPath()));
    }

    @Test
    @DisplayName("ImproperlyConfiguredValueException with an expected type mentions it too")
    void testImproperlyConfiguredWithExpecting() {
        ImproperlyConfiguredValueException exc = new ImproperlyConfiguredValueException(null, "my-option", "an integer");
        assertTrue(exc.getMessage().contains("my-option"));
        assertTrue(exc.getMessage().contains("an integer"));
    }

    @Test
    @DisplayName("InvalidConfigException preserves message and optional cause")
    void testInvalidConfigException() {
        InvalidConfigException noCause = new InvalidConfigException("bad value");
        assertTrue(noCause.getMessage().contains("bad value"));

        RuntimeException cause = new RuntimeException("root cause");
        InvalidConfigException withCause = new InvalidConfigException("bad value", cause);
        assertSame(cause, withCause.getCause());
    }

    @Test
    @DisplayName("UnrecognizedConfigValueException mentions the class and path")
    void testUnrecognizedConfigValueException() {
        UnrecognizedConfigValueException exc = new UnrecognizedConfigValueException(String.class, "some.path");
        assertTrue(exc.getMessage().contains("String"));
        assertTrue(exc.getMessage().contains("some.path"));
    }
}
