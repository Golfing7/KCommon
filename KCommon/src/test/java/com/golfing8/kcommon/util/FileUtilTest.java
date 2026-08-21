package com.golfing8.kcommon.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilTest {

    private static String readFile(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static Path buildTestJar(Path dir) throws IOException {
        Path jarPath = dir.resolve("test.jar");
        try (JarOutputStream jarOut = new JarOutputStream(Files.newOutputStream(jarPath))) {
            writeEntry(jarOut, "resources/", null);
            writeEntry(jarOut, "resources/file1.txt", "Hello, world!");
            writeEntry(jarOut, "resources/nested/", null);
            writeEntry(jarOut, "resources/nested/file2.txt", "Nested contents");
            writeEntry(jarOut, "other/file3.txt", "Should not be copied");
        }
        return jarPath;
    }

    private static void writeEntry(JarOutputStream jarOut, String name, String content) throws IOException {
        JarEntry entry = new JarEntry(name);
        jarOut.putNextEntry(entry);
        if (content != null) {
            jarOut.write(content.getBytes(StandardCharsets.UTF_8));
        }
        jarOut.closeEntry();
    }

    @Nested
    @DisplayName("copyJarElements")
    class CopyJarElements {

        @Test
        @DisplayName("Throws when the jar file does not exist")
        void testMissingJarThrows(@TempDir Path tempDir) {
            Path missingJar = tempDir.resolve("does-not-exist.jar");
            Path destination = tempDir.resolve("out");

            assertThrows(IllegalArgumentException.class,
                    () -> FileUtil.copyJarElements(missingJar, "resources", destination));
        }

        @Test
        @DisplayName("Copies only files under the requested jar directory, preserving structure")
        void testCopiesOnlyRequestedDirectory(@TempDir Path tempDir) throws IOException {
            Path jarPath = buildTestJar(tempDir);
            Path destination = tempDir.resolve("out");

            FileUtil.copyJarElements(jarPath, "resources", destination);

            Path file1 = destination.resolve("file1.txt");
            Path nestedFile2 = destination.resolve("nested/file2.txt");
            assertTrue(Files.exists(file1));
            assertEquals("Hello, world!", readFile(file1));
            assertTrue(Files.exists(nestedFile2));
            assertEquals("Nested contents", readFile(nestedFile2));

            assertFalse(Files.exists(destination.resolve("file3.txt")));
        }

        @Test
        @DisplayName("Does not overwrite files that already exist at the destination")
        void testDoesNotOverwriteExistingFile(@TempDir Path tempDir) throws IOException {
            Path jarPath = buildTestJar(tempDir);
            Path destination = tempDir.resolve("out");
            Files.createDirectories(destination);
            Files.write(destination.resolve("file1.txt"), "Existing contents".getBytes(StandardCharsets.UTF_8));

            FileUtil.copyJarElements(jarPath, "resources", destination);

            assertEquals("Existing contents", readFile(destination.resolve("file1.txt")));
        }

        @Test
        @DisplayName("Creates the destination directory if it does not already exist")
        void testCreatesDestinationDirectory(@TempDir Path tempDir) throws IOException {
            Path jarPath = buildTestJar(tempDir);
            Path destination = tempDir.resolve("nested/does/not/exist");

            FileUtil.copyJarElements(jarPath, "resources", destination);

            assertTrue(Files.isDirectory(destination));
            assertTrue(Files.exists(destination.resolve("file1.txt")));
        }
    }
}
