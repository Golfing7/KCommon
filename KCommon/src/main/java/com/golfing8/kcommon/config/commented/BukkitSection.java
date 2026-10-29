package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.config.KSection;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.config.exc.ConfigException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Lets a plain Bukkit {@link ConfigurationSection} be used where KCommon expects a {@link KSection}.
 * <p>
 * KCommon's own config sections already are {@link KSection}s. This is only for sections that come
 * straight from Bukkit, such as a {@code YamlConfiguration} loaded by hand.
 * </p>
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BukkitSection implements KSection {
    /**
     * The Bukkit section this wraps.
     */
    @Getter
    private final ConfigurationSection handle;

    /**
     * Gets the given section as a {@link KSection}, wrapping it only if needed.
     *
     * @param section the section.
     * @return the section as a KSection, or null if the section was null.
     */
    public static @Nullable KSection of(@Nullable ConfigurationSection section) {
        if (section == null)
            return null;

        return section instanceof KSection ? (KSection) section : new BukkitSection(section);
    }

    /**
     * Gets the Bukkit section behind the given section.
     *
     * @param section the section.
     * @return the Bukkit section, or null if the section was null.
     */
    public static @Nullable ConfigurationSection unwrap(@Nullable KSection section) {
        if (section == null || section instanceof ConfigurationSection)
            return (ConfigurationSection) section;

        if (section instanceof BukkitSection)
            return ((BukkitSection) section).handle;

        throw new IllegalArgumentException("Not a Bukkit section: " + section.getClass());
    }

    @Override
    public Object get(String path) {
        Object value = handle.get(path);
        return value instanceof ConfigurationSection ? of((ConfigurationSection) value) : value;
    }

    @Override
    public Object get(String path, Object def) {
        return handle.get(path, def);
    }

    @Override
    public void set(String path, Object value) {
        handle.set(path, value instanceof BukkitSection ? ((BukkitSection) value).handle : value);
    }

    @Override
    public boolean contains(String path) {
        return handle.contains(path);
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        return handle.getKeys(deep);
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return handle.isConfigurationSection(path);
    }

    @Override
    public @Nullable KSection getConfigurationSection(String path) {
        return of(handle.getConfigurationSection(path));
    }

    @Override
    public KSection createSection(String path) {
        return of(handle.createSection(path));
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public String getCurrentPath() {
        return handle.getCurrentPath();
    }

    @Override
    public KSection getRootSection() {
        return of(handle.getRoot());
    }

    @Override
    public String getString(String path) {
        return handle.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return handle.getString(path, def);
    }

    @Override
    public int getInt(String path) {
        return handle.getInt(path);
    }

    @Override
    public int getInt(String path, int def) {
        return handle.getInt(path, def);
    }

    @Override
    public long getLong(String path) {
        return handle.getLong(path);
    }

    @Override
    public double getDouble(String path) {
        return handle.getDouble(path);
    }

    @Override
    public boolean getBoolean(String path) {
        return handle.getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return handle.getBoolean(path, def);
    }

    @Override
    public List<?> getList(String path) {
        return handle.getList(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return handle.getStringList(path);
    }

    @Override
    public void set(String path, Object value, String... comments) {
        set(path, value instanceof ConfigPrimitive ? ((ConfigPrimitive) value).unwrap() : value);
    }

    @Override
    public void setComments(String path, String... comments) {
        // Plain Bukkit sections don't keep comments.
    }

    @Override
    public void ensureExists(String path) {
        if (!contains(path))
            throw new ConfigException(this, "Config entry at path " + path + " doesn't exist!");
    }

    @Override
    public boolean tryLoadFromSource(String path) {
        return contains(path);
    }

    @Override
    public boolean isModified() {
        return false;
    }
}
