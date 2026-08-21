package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.module.Module;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MConfigurationTest {

    @Test
    @DisplayName("Exposes the module it was constructed with, and behaves like a normal Configuration otherwise")
    void testExposesModuleAndBehavesAsConfiguration(@TempDir Path tempDir) {
        Module module = mock(Module.class);
        when(module.getModuleName()).thenReturn("test-module");

        MConfiguration configuration = new MConfiguration(tempDir.resolve("config.yml"), module);
        assertSame(module, configuration.getModule());

        configuration.set("key", "value");
        assertEquals("value", configuration.getString("key"));
        assertEquals("config", configuration.getFileNameNoExtension());
    }
}
