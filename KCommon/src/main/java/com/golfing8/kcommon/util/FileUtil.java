package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipInputStream;

@UtilityClass
public final class FileUtil {

    /**
     * Copies all the jar elements from the jar to the given destination path.
     *
     * @param pathToJar the path to the jar file.
     * @param jarDirectory the jar directory to pull elements from.
     * @param destination the destination directory.
     * @throws IOException if any IO shenanigans occur.
     */
    public static void copyJarElements(Path pathToJar, String jarDirectory, Path destination) throws IOException {
        if (Files.notExists(pathToJar)) {
            throw new IllegalArgumentException("Jar file does not exist at path: " + pathToJar);
        }

        Files.createDirectories(destination);
        try (JarFile jarFile = new JarFile(pathToJar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (!jarEntry.getName().startsWith(jarDirectory + "/"))
                    continue;

                String fileName = jarEntry.getName().replace(jarDirectory + "/", "");
                // Skip directories.
                if (fileName.endsWith("/"))
                    continue;

                Path newFilePath = destination.resolve(fileName);
                if (Files.exists(newFilePath))
                    continue;

                // If the file already exists as a normal file, don't touch it.
                if (Files.isRegularFile(newFilePath.getParent())) {
                    continue;
                }

                Files.createDirectories(newFilePath.getParent());
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                try (FileWriter writer = new FileWriter(newFilePath.toFile())) {
                    IOUtils.copy(inputStream, writer);
                }
            }
        }
    }
}
