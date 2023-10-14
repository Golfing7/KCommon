package com.golfing8.kcommon.config.commented;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * A bukkit {@link Configuration} that supports comments.
 * <p>
 * Along with comments, other convenience methods have been added.
 * </p>
 */
public interface Config extends org.bukkit.configuration.Configuration {

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
     * Gets the value at the path with the given type.
     *
     * @param path the path.
     * @param type the type.
     * @return the value.
     */
    Object getWithType(String path, Field type);
}
