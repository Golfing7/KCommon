package com.golfing8.kcommon.menu;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Keeps {@code kcommon/menu-schema.json}, which the IntelliJ plugin reads, in step with {@link MenuSchema}.
 * When they differ the test rewrites the file and fails, so the new file only needs committing.
 */
class MenuSchemaJsonTest {
    private static final Path FILE = Paths.get("src/main/resources/kcommon/menu-schema.json");

    @Test
    void schemaFileIsUpToDate() throws IOException {
        String expected = MenuSchema.toJson();
        String actual = Files.exists(FILE) ? new String(Files.readAllBytes(FILE), StandardCharsets.UTF_8) : "";
        if (!expected.equals(actual)) {
            Files.write(FILE, expected.getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(expected, actual, FILE + " was out of date and has been rewritten; commit the new file");
    }
}
