package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.module.test.util.FakeServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LangConfigTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    @Test
    @DisplayName("Adding a new constant creates the file and stores the message")
    void testAddNewConstant(@TempDir Path tempDir) {
        Path configPath = tempDir.resolve("lang.yml");
        LangConfig config = new LangConfig(configPath);
        config.load();

        boolean added = config.addLanguageConstant("some-key", Arrays.asList("line1", "line2"));
        assertTrue(added);
        assertEquals(Arrays.asList("line1", "line2"), config.getMessage("some-key").getMessages());
        assertTrue(configPath.toFile().exists());
    }

    @Test
    @DisplayName("Adding a constant that already exists on disk loads the existing value instead")
    void testAddExistingConstantLoadsFromDisk(@TempDir Path tempDir) {
        Path configPath = tempDir.resolve("lang.yml");
        LangConfig config = new LangConfig(configPath);
        config.load();
        config.addLanguageConstant("greeting", Collections.singletonList("original"));
        config.save();

        // A second, fresh LangConfig instance loaded from the same file should see the persisted value.
        LangConfig second = new LangConfig(configPath);
        second.load();
        boolean added = second.addLanguageConstant("greeting", Collections.singletonList("different-default"));
        assertFalse(added);
        assertEquals(Collections.singletonList("original"), second.getMessage("greeting").getMessages());
    }

    @Test
    @DisplayName("Rejects keys that don't match the expected key format")
    void testRejectsInvalidKeyFormat(@TempDir Path tempDir) {
        Path configPath = tempDir.resolve("lang.yml");
        LangConfig config = new LangConfig(configPath);
        config.load();

        assertThrows(IllegalArgumentException.class,
                () -> config.addLanguageConstant("Not A Valid Key!!", Collections.singletonList("x")));
    }

    @Test
    @DisplayName("getMessage returns null for an unknown key")
    void testGetUnknownMessage(@TempDir Path tempDir) {
        LangConfig config = new LangConfig(tempDir.resolve("lang.yml"));
        config.load();
        assertNull(config.getMessage("does-not-exist"));
    }
}
