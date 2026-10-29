package com.golfing8.kcommon.config;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * A section of a YAML config, independent of the platform KCommon runs on.
 * <p>
 * Method signatures match Bukkit's {@code KSection} so that KCommon's Bukkit sections
 * implement this interface without adapters.
 * </p>
 */
public interface KSection {
    /**
     * Gets the value at the path, or null if it isn't set.
     */
    Object get(String path);

    /**
     * Gets the value at the path, or the default if it isn't set.
     */
    Object get(String path, Object def);

    /**
     * Sets the value at the path. A null value removes it.
     */
    void set(String path, Object value);

    /**
     * Checks if a value is set at the path.
     */
    boolean contains(String path);

    /**
     * Gets the keys of this section, including the keys of nested sections if deep.
     */
    Set<String> getKeys(boolean deep);

    /**
     * Checks if the value at the path is a section.
     */
    boolean isConfigurationSection(String path);

    /**
     * Gets the section at the path, or null if there is none.
     */
    @Nullable
    KSection getConfigurationSection(String path);

    /**
     * Creates an empty section at the path, replacing what was there.
     */
    KSection createSection(String path);

    /**
     * Gets the name of this section, the last element of its path.
     */
    String getName();

    /**
     * Gets the full path of this section from the root.
     */
    String getCurrentPath();

    /**
     * Gets the root of the config this section belongs to.
     * Not named {@code getRoot} because Bukkit's {@code getRoot} returns a Bukkit type.
     *
     * @return the root section.
     */
    KSection getRootSection();

    /**
     * Gets the value at the path as a string, or null.
     */
    String getString(String path);

    /**
     * Gets the value at the path as a string, or the default.
     */
    String getString(String path, String def);

    /**
     * Gets the value at the path as an int, or 0.
     */
    int getInt(String path);

    /**
     * Gets the value at the path as an int, or the default.
     */
    int getInt(String path, int def);

    /**
     * Gets the value at the path as a long, or 0.
     */
    long getLong(String path);

    /**
     * Gets the value at the path as a double, or 0.
     */
    double getDouble(String path);

    /**
     * Gets the boolean at the path, or false.
     */
    boolean getBoolean(String path);

    /**
     * Gets the boolean at the path, or the default.
     */
    boolean getBoolean(String path, boolean def);

    /**
     * Gets the list at the path, or null.
     */
    List<?> getList(String path);

    /**
     * Gets the list at the path as strings, skipping elements that aren't simple values.
     */
    List<String> getStringList(String path);

    /**
     * Sets the value at the given path with the given comments.
     *
     * @param path     the path.
     * @param value    the value.
     * @param comments the comments.
     */
    void set(String path, Object value, String... comments);

    /**
     * Sets the comments at the given path.
     *
     * @param path     the path.
     * @param comments the comments.
     */
    void setComments(String path, String... comments);

    /**
     * Ensures the given path exists, copying it from the source config if needed.
     *
     * @param path the path.
     */
    void ensureExists(String path);

    /**
     * Tries to load the given path from the source config if it isn't present.
     *
     * @param path the path.
     * @return true if the path exists after this call.
     */
    boolean tryLoadFromSource(String path);

    /**
     * Checks if this section was modified since it was last saved.
     *
     * @return true if modified.
     */
    boolean isModified();

    /**
     * Gets the value under the given path with the given type
     *
     * @param path the path
     * @param type the type
     * @return the value
     */
    default <T> Optional<T> get(String path, Class<T> type) {
        if (!this.contains(path)) {
            return Optional.empty();
        }

        return Optional.ofNullable(ConfigTypeRegistry.getFromType(new ConfigEntry(this, path), type));
    }

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

    /**
     * Gets or loads the value under the given path with the given type.
     *
     * @param path the path
     * @param type the type
     * @return the loaded value
     */
    default <T> Optional<T> getOrLoad(String path, TypeToken<T> type) {
        if (!this.tryLoadFromSource(path))
            return Optional.empty();

        return Optional.ofNullable(ConfigTypeRegistry.getFromType(new ConfigEntry(this, path), FieldType.extractFrom(type)));
    }

    /**
     * Gets or loads the value under the given path with the given type.
     *
     * @param path the path
     * @param type the type
     * @return the loaded value
     */
    default <T> Optional<T> getOrLoad(String path, FieldType type) {
        if (!this.tryLoadFromSource(path))
            return Optional.empty();

        return Optional.ofNullable(ConfigTypeRegistry.getFromType(new ConfigEntry(this, path), type));
    }
}
