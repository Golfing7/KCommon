package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.config.KSection;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides extra getters for KCommon structures.
 */
public interface KConfigurationSection extends ConfigurationSection, KSection {
    /**
     * Sets the value at the given path with the given comments.
     *
     * @param path     the path.
     * @param value    the value.
     * @param comments the comments.
     */
    void set(String path, Object value, String... comments);

    /**
     * Sets the comments of the given path.
     *
     * @param path     the path to set.
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
     * Checks if this config tied to this configuration section has been modified
     *
     * @return true if modified
     */
    boolean isModified();

    /**
     * Gets the KConfigurationSection under the given path
     *
     * @param path the path
     * @return the section
     */
    @Nullable
    KConfigurationSection getConfigurationSection(String path);

    @Override
    KConfigurationSection createSection(String path);

    @Override
    default KSection getRootSection() {
        org.bukkit.configuration.Configuration root = getRoot();
        return root instanceof KSection ? (KSection) root : BukkitSection.of(root);
    }

    /**
     * An optional variant of {@link #getConfigurationSection(String)}
     *
     * @param path the path
     * @return the optional section
     */
    default Optional<KConfigurationSection> getConfigurationSectionOpt(String path) {
        return Optional.ofNullable(getConfigurationSection(path));
    }

    /**
     * Iterates through every subsection in this section.
     *
     * @param action the action.
     */
    default void forEachSubsection(Consumer<? super KConfigurationSection> action) {
        forEachSubsection("", action);
    }

    /**
     * Iterates through every subsection in this section.
     *
     * @param action the action.
     */
    default void forEachSubsection(String path, Consumer<? super KConfigurationSection> action) {
        KConfigurationSection section = this.getConfigurationSection(path);
        if (section == null)
            return;

        for (String key : section.getKeys(false)) {
            if (!section.isConfigurationSection(key))
                continue;

            action.accept(section.getConfigurationSection(key));
        }
    }

    /**
     * Iterates through every key in this section.
     *
     * @param action the action.
     */
    default void forEachKey(Consumer<? super String> action) {
        this.forEachKey("", action);
    }

    /**
     * Iterates through every key under the given path.
     * If the path does not exist or is not a configuration section, nothing will happen.
     *
     * @param path   the path
     * @param action the action applied to the path. The provided path is relative to THIS section.
     */
    default void forEachKey(String path, Consumer<? super String> action) {
        KConfigurationSection section = this.getConfigurationSection(path);
        if (section == null)
            return;

        for (String key : section.getKeys(false)) {
            action.accept(!path.isEmpty() ? path + "." + key : key);
        }
    }
}
