package com.golfing8.kcommon.config.yaml;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class YamlConfigTest {
    @Test
    void keepsCommentsAndValuesThroughASaveAndLoad(@TempDir Path dir) {
        YamlConfig config = new YamlConfig(dir.resolve("config.yml"));
        config.loadFromString("# Top comment\nsome-integer-option: 100\nnested:\n  # Inner comment\n  name: Hello World! # trailing\n  list:\n  - a\n  - b\n");

        assertEquals(100, config.getInt("some-integer-option"));
        assertEquals("Hello World!", config.getString("nested.name"));
        assertEquals(Arrays.asList("a", "b"), config.getStringList("nested.list"));
        assertTrue(config.isConfigurationSection("nested"));
        assertEquals("nested", config.getConfigurationSection("nested").getCurrentPath());

        config.set("nested.added", 5, "Added value");
        config.save();

        YamlConfig reloaded = new YamlConfig(dir.resolve("config.yml"));
        reloaded.load();
        assertEquals(5, reloaded.getInt("nested.added"));
        assertEquals("Hello World!", reloaded.getString("nested.name"));

        String saved = reloaded.saveToString();
        assertTrue(saved.contains("# Top comment"), saved);
        assertTrue(saved.contains("# Inner comment"), saved);
        assertTrue(saved.contains("# Added value"), saved);
        assertTrue(saved.contains("# trailing"), saved);
    }

    @Test
    void fallsBackOnItsSource(@TempDir Path dir) {
        YamlConfig source = new YamlConfig();
        source.loadFromString("missing: 3\n");
        YamlConfig config = new YamlConfig(dir.resolve("config.yml"));
        config.loadFromString("");
        config.setSource(source);

        assertTrue(config.tryLoadFromSource("missing"));
        assertEquals(3, config.getInt("missing"));
        assertFalse(config.tryLoadFromSource("absent"));
    }
}
