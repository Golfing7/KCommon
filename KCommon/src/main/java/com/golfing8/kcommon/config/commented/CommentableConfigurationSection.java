package com.golfing8.kcommon.config.commented;

import org.bukkit.configuration.ConfigurationSection;

public interface CommentableConfigurationSection extends ConfigurationSection {
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
}
