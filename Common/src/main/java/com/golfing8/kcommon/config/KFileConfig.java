package com.golfing8.kcommon.config;

import com.golfing8.kcommon.module.AbstractModule;
import org.jetbrains.annotations.Nullable;

/**
 * A config that is backed by a file.
 */
public interface KFileConfig extends KSection {
    /**
     * Gets the file name of this config without its extension.
     *
     * @return the file name.
     */
    String getFileNameNoExtension();

    /**
     * Saves this config to its file.
     */
    void save();

    /**
     * Gets the module this config belongs to.
     *
     * @return the module, or null if it doesn't belong to one.
     */
    default @Nullable AbstractModule<?> getModule() {
        return null;
    }
}
