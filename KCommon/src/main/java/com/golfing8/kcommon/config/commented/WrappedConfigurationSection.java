package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import lombok.AllArgsConstructor;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
public class WrappedConfigurationSection implements ConfigurationSection {
    private final ConfigurationSection wrapped;
    private final com.golfing8.kcommon.config.commented.Configuration originalConfig;

    @Override
    public Object get(String path) {
        return wrapped.get(path);
    }

    @Override
    public Object get(String path, Object def) {
        return wrapped.get(path, def);
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public boolean getBoolean(String path) {
        return wrapped.getBoolean(path);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        return wrapped.getBoolean(path, def);
    }

    @Override
    public boolean isBoolean(String s) {
        return wrapped.isBoolean(s);
    }

    @Override
    public Color getColor(String path) {
        return wrapped.getColor(path);
    }

    @Override
    public Color getColor(String path, Color def) {
        return wrapped.getColor(path, def);
    }

    @Override
    public boolean isColor(String s) {
        return wrapped.isColor(s);
    }

    @Override
    public ConfigurationSection getParent() {
        return new WrappedConfigurationSection(wrapped.getParent(), originalConfig);
    }

    @Override
    public org.bukkit.configuration.Configuration getRoot() {
        return originalConfig;
    }

    @Override
    public ConfigurationSection getDefaultSection() {
        return new WrappedConfigurationSection(originalConfig.getDefaultSection(), originalConfig);
    }

    @Override
    public double getDouble(String path) {
        return wrapped.getDouble(path);
    }

    @Override
    public double getDouble(String path, double def) {
        return wrapped.getDouble(path, def);
    }

    @Override
    public boolean isDouble(String s) {
        return wrapped.isDouble(s);
    }

    @Override
    public float getFloat(String s) {
        return wrapped.getFloat(s);
    }

    @Override
    public float getFloat(String s, float v) {
        return wrapped.getFloat(s, v);
    }

    @Override
    public boolean isFloat(String s) {
        return wrapped.isFloat(s);
    }

    @Override
    public ConfigurationSection getConfigurationSection(String path) {
        return new WrappedConfigurationSection(wrapped.getConfigurationSection(path), originalConfig);
    }

    @Override
    public boolean isConfigurationSection(String s) {
        return wrapped.isConfigurationSection(s);
    }

    @Override
    public int getInt(String path, int def) {
        return wrapped.getInt(path, def);
    }

    @Override
    public boolean isInt(String s) {
        return wrapped.isInt(s);
    }

    @Override
    public int getInt(String path) {
        return wrapped.getInt(path);
    }

    @Override
    public ItemStack getItemStack(String path) {
        return wrapped.getItemStack(path);
    }

    @Override
    public ItemStack getItemStack(String path, ItemStack def) {
        return wrapped.getItemStack(path, def);
    }

    @Override
    public boolean isItemStack(String s) {
        return wrapped.isItemStack(s);
    }

    @Override
    public List<?> getList(String path) {
        return wrapped.getList(path);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        return wrapped.getList(path, def);
    }

    @Override
    public boolean isList(String s) {
        return wrapped.isList(s);
    }

    @Override
    public List<Boolean> getBooleanList(String path) {
        return wrapped.getBooleanList(path);
    }

    @Override
    public List<Byte> getByteList(String path) {
        return wrapped.getByteList(path);
    }

    @Override
    public List<Character> getCharacterList(String path) {
        return wrapped.getCharacterList(path);
    }

    @Override
    public List<Double> getDoubleList(String path) {
        return wrapped.getDoubleList(path);
    }

    @Override
    public List<Float> getFloatList(String path) {
        return wrapped.getFloatList(path);
    }

    @Override
    public List<Integer> getIntegerList(String path) {
        return wrapped.getIntegerList(path);
    }

    @Override
    public List<Long> getLongList(String path) {
        return wrapped.getLongList(path);
    }

    @Override
    public long getLong(String path) {
        return wrapped.getLong(path);
    }

    @Override
    public long getLong(String path, long def) {
        return wrapped.getLong(path, def);
    }

    @Override
    public boolean isLong(String s) {
        return wrapped.isLong(s);
    }

    @Override
    public List<Map<?, ?>> getMapList(String path) {
        return wrapped.getMapList(path);
    }

    @Override
    public List<Short> getShortList(String path) {
        return wrapped.getShortList(path);
    }

    @Override
    public List<String> getStringList(String path) {
        return wrapped.getStringList(path);
    }

    @Override
    public Map<String, Object> getValues(boolean deep) {
        return wrapped.getValues(deep);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String path) {
        return wrapped.getOfflinePlayer(path);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String path, OfflinePlayer def) {
        return wrapped.getOfflinePlayer(path, def);
    }

    @Override
    public boolean isOfflinePlayer(String s) {
        return wrapped.isOfflinePlayer(s);
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        return wrapped.getKeys(deep);
    }

    @Override
    public String getCurrentPath() {
        return wrapped.getCurrentPath();
    }

    @Override
    public String getString(String path) {
        return wrapped.getString(path);
    }

    @Override
    public String getString(String path, String def) {
        return wrapped.getString(path, def);
    }

    @Override
    public boolean isString(String s) {
        return wrapped.isString(s);
    }

    @Override
    public org.bukkit.util.Vector getVector(String path) {
        return wrapped.getVector(path);
    }

    @Override
    public org.bukkit.util.Vector getVector(String path, org.bukkit.util.Vector def) {
        return wrapped.getVector(path, def);
    }

    @Override
    public boolean isVector(String s) {
        return wrapped.isVector(s);
    }

    @Override
    public void set(String path, Object value) {
        ConfigTypeRegistry.setInConfig(wrapped, path, value);
    }

    @Override
    public boolean isSet(String path) {
        return wrapped.isSet(path);
    }

    @Override
    public boolean contains(String path) {
        return wrapped.contains(path);
    }

    @Override
    public void addDefault(String path, Object value) {
        wrapped.addDefault(path, value);
    }

    @Override
    public ConfigurationSection createSection(String path) {
        return new WrappedConfigurationSection(wrapped.createSection(path), originalConfig);
    }

    @Override
    public ConfigurationSection createSection(String path, Map<?, ?> map) {
        return new WrappedConfigurationSection(wrapped.createSection(path, map), originalConfig);
    }
}
