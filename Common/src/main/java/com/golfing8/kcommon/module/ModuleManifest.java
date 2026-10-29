package com.golfing8.kcommon.module;

import com.golfing8.kcommon.data.serializer.DataSerializer;
import lombok.Getter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A manifest for containing module information for an instance of {@code KPlugin}.
 * <p>
 * This primarily contains information on whether a module is enabled or not.
 * </p>
 */
public class ModuleManifest {
    /**
     * The states of all modules controlled by the plugin
     */
    @Getter
    private final Map<String, Boolean> moduleStates = new HashMap<>();

    /**
     * Called when a module is registering itself into the {@link Modules} class.
     *
     * @param module the module.
     * @return if the module is marked for enabling/disabling.
     */
    public boolean loadModule(AbstractModule<?> module) {
        if (!moduleStates.containsKey(module.getModuleName())) {
            moduleStates.put(module.getModuleName(), true);
            return true;
        }
        return moduleStates.get(module.getModuleName());
    }

    /**
     * Sets the active status of the given module
     *
     * @param module the module.
     * @param active if the module is active.
     */
    public void setActive(AbstractModule<?> module, boolean active) {
        if (!moduleStates.containsKey(module.getModuleName()))
            throw new IllegalArgumentException(String.format("Module %s not registered to this manifest.", module.getModuleName()));

        moduleStates.put(module.getModuleName(), active);
    }

    /**
     * Checks if the given module is active.
     *
     * @param module te module.
     * @return if its active
     */
    public boolean isActive(AbstractModule<?> module) {
        if (!moduleStates.containsKey(module.getModuleName()))
            throw new IllegalArgumentException(String.format("Module %s not registered to this manifest.", module.getModuleName()));

        return moduleStates.get(module.getModuleName());
    }

    /**
     * Loads the manifest stored in the given file, creating the file if it doesn't exist.
     *
     * @param path   the file.
     * @param logger the logger to report problems to.
     * @return the manifest, or an empty manifest if it couldn't be read.
     */
    public static ModuleManifest load(Path path, Logger logger) {
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException exc) {
                logger.log(Level.SEVERE, "Failed to save default module-manifest.json!", exc);
            }
            return new ModuleManifest();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            ModuleManifest loadedManifest = DataSerializer.getGSONBase().fromJson(reader, ModuleManifest.class);
            return loadedManifest == null || loadedManifest.getModuleStates() == null ? new ModuleManifest() : loadedManifest;
        } catch (IOException exc) {
            logger.log(Level.SEVERE, "Failed to read module-manifest.json! Loading default manifest...", exc);
            return new ModuleManifest();
        }
    }

    /**
     * Saves this manifest to the given file.
     *
     * @param path   the file.
     * @param logger the logger to report problems to.
     */
    public void save(Path path, Logger logger) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            DataSerializer.getGSONBase().toJson(this, writer);
        } catch (IOException exc) {
            logger.log(Level.SEVERE, "Failed to save module-manifest.json!", exc);
        }
    }
}
