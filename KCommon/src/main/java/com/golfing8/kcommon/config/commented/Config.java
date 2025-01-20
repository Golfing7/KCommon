package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * A bukkit {@link Configuration} that supports comments.
 * <p>
 * Along with comments, other convenience methods have been added.
 * </p>
 */
public interface Config extends org.bukkit.configuration.Configuration, CommentableConfigurationSection {

    /**
     * Ensures the given path exists.
     *
     * @param path the path.
     */
    void ensureExists(String path);

    /**
     * Gets the source config that this config should mirror.
     *
     * @return the source config.
     */
    YamlConfiguration getSource();

    /**
     * Sets the source config.
     *
     * @param config the source config.
     */
    void setSource(YamlConfiguration config);

    /**
     * Gets the value at the path with the given type.
     *
     * @param path the path.
     * @param type the type.
     * @return the value.
     */
    default Object getWithType(String path, Field type) {
        return getWithType(path, new FieldType(type));
    }

    /**
     * Gets the value at the path with the given type.
     *
     * @param path the path.
     * @param type the type.
     * @return the value.
     */
    default Object getWithType(String path, Class<?> type) {
        return getWithType(path, new FieldType(type));
    }

    /**
     * Gets the value at the path with the given type.
     *
     * @param path the path.
     * @param type the type.
     * @return the value.
     */
    Object getWithType(String path, FieldType type);
}
