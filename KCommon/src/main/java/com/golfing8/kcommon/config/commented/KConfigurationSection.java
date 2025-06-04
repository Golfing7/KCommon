package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.menu.MenuBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Provides extra getters for KCommon structures.
 */
public interface KConfigurationSection extends ConfigurationSection {
    /**
     * Sets the value at the given path with the given comments.
     *
     * @param path the path.
     * @param value the value.
     * @param comments the comments.
     */
    void set(String path, Object value, String... comments);

    /**
     * Sets the comments of the given path.
     *
     * @param path the path to set.
     * @param comments the comments to set, or null if they should be cleared.
     */
    void setComments(String path, String... comments);

    /**
     * Ensures the given path exists, throwing a {@link com.golfing8.kcommon.config.exc.ConfigException} if not found.
     *
     * @param path the path.
     */
    void ensureExists(String path);

    /**
     * Tries to load the given path from source if it doesn't exist.
     *
     * @param path the path
     * @return true if the path now exists
     */
    boolean tryLoadFromSource(String path);

    /**
     * Gets or loads the value under the given path with the given type.
     *
     * @param path the path
     * @param type the type
     * @return the loaded value
     */
    default <T> Optional<T> getOrLoad(String path, Class<T> type) {
        if (!this.tryLoadFromSource(path))
            return Optional.empty();

        return Optional.ofNullable(ConfigTypeRegistry.getFromType(new ConfigEntry(this, path), type));
    }
}
