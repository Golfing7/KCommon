package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Color;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents a {@link YamlConfiguration} with the capability of saving comments.
 * <p>
 * Other QOL features are included as well such as alphabetic saving.
 * </p>
 */
public class Configuration extends YamlConfiguration implements Config {
    /** The path to the config file */
    private final Path configPath;
    /** The config file we're wrapping */
    @Getter
    private final YamlConfiguration wrapped;
    /** The comments of each mapped value */
    @Getter
    private final Map<String, String[]> comments;

    /** The source configuration that this configuration should mirror */
    @Getter @Setter
    private YamlConfiguration source;

    public Configuration(Path configPath) {
        this.configPath = configPath;
        this.comments = new HashMap<>();
        this.wrapped = new YamlConfiguration();
    }

    /**
     * Loads this config from a string.
     *
     * @param data the data.
     */
    @Override
    public void loadFromString(String data) {
        try {
            this.comments.clear();
            this.wrapped.loadFromString(data);
            loadComments(data);
        } catch (InvalidConfigurationException exc) {
            exc.printStackTrace();
            KCommon.getInstance().getLogger().warning(String.format("Failed to load config file at location %s!", this.configPath));
            throw new RuntimeException(exc);
        }
    }

    @Override
    public void ensureExists(String path) {
        if (this.source == null || this.contains(path) || !this.source.contains(path))
            return;

        this.set(path, this.source.get(path));
        this.save();
    }

    /**
     * Loads the internal YamlConfiguration.
     */
    public void load() {
        try {
            this.comments.clear();
            String fileString = String.join("\n", Files.readAllLines(this.configPath));
            this.wrapped.loadFromString(fileString);
            loadComments(fileString);
        } catch (IOException | InvalidConfigurationException exc) {
            KCommon.getInstance().getLogger().warning(String.format("Failed to load config file at location %s!", this.configPath));
            throw new RuntimeException(exc);
        }
    }

    @Override
    public String saveToString() {
        String yamlString = this.wrapped.saveToString();
        ConfigTransformer transformer = new ConfigTransformer(yamlString);
        for (String key : transformer) {
            if (comments.containsKey(key))
                transformer.insertComment(comments.get(key));
        }

        return String.join("\n", transformer.getTransformedLines());
    }

    /**
     * Saves this configuration, including the comments.
     */
    public void save() {
        try {
            Files.write(configPath, saveToString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to save config at path %s!", this.configPath), exc);
        }
    }

    /**
     * Loads the comments from the config.
     *
     * @param configString the config string.
     */
    private void loadComments(String configString) {
        ConfigTransformer transformer = new ConfigTransformer(configString);
        for (String key : transformer) {
            comments.put(key, transformer.getJunk().stream().map(StringUtils::strip).filter(str -> str.startsWith("#")).toArray(String[]::new));
        }
    }

    @Override
    public void set(String path, Object value, String... comments) {
        setComments(path, comments);
        set(path, value);
    }

    @Override
    public void setComments(String path, String... comments) {
        if (comments == null || comments.length == 0) {
            this.comments.remove(path);
        } else {
            this.comments.put(path, Arrays.stream(comments).map(str -> "# " + str).toArray(String[]::new));
        }
    }

    // BOILERPLATE

    @Override
    public Object get(String path) {
        Object out = wrapped.get(path);
        if (out instanceof ConfigurationSection && !(out instanceof WrappedConfigurationSection)) {
            return new WrappedConfigurationSection((ConfigurationSection) out, this);
        }
        return out;
    }

    @Override
    public Object getWithType(String path, FieldType type) {
        return ConfigTypeRegistry.getFromType(new ConfigEntry(this, path), type);
    }

    @Override
    public Object get(String path, Object def) {
        return wrapped.get(path, def);
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    public String getFileName() {
        return this.configPath.getFileName().toString();
    }

    public String getFileNameNoExtension() {
        return this.configPath.getFileName().toString().replace(".yml", "");
    }

    @Override
    protected Object getDefault(String path) {
        throw new UnsupportedOperationException();
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
    public Color getColor(String path) {
        return wrapped.getColor(path);
    }

    @Override
    public Color getColor(String path, Color def) {
        return wrapped.getColor(path, def);
    }

    @Override
    public ConfigurationSection getParent() {
        return new WrappedConfigurationSection(wrapped.getParent(), this);
    }

    @Override
    public org.bukkit.configuration.Configuration getDefaults() {
        return wrapped.getDefaults();
    }

    @Override
    public org.bukkit.configuration.Configuration getRoot() {
        return this;
    }

    @Override
    public ConfigurationSection getDefaultSection() {
        return new WrappedConfigurationSection(wrapped.getDefaultSection(), this);
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
    public ConfigurationSection getConfigurationSection(String path) {
        return new WrappedConfigurationSection(wrapped.getConfigurationSection(path), this);
    }

    @Override
    public int getInt(String path, int def) {
        return wrapped.getInt(path, def);
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
    public List<?> getList(String path) {
        return wrapped.getList(path);
    }

    @Override
    public List<?> getList(String path, List<?> def) {
        return wrapped.getList(path, def);
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
    public org.bukkit.util.Vector getVector(String path) {
        return wrapped.getVector(path);
    }

    @Override
    public org.bukkit.util.Vector getVector(String path, org.bukkit.util.Vector def) {
        return wrapped.getVector(path, def);
    }

    @Override
    public void set(String path, Object value) {
        ConfigTypeRegistry.setInConfig(wrapped, path, value);
    }

    @Override
    public void setDefaults(org.bukkit.configuration.Configuration defaults) {
        wrapped.setDefaults(defaults);
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
    public void addDefaults(Map<String, Object> defaults) {
        wrapped.addDefaults(defaults);
    }

    @Override
    public void addDefaults(org.bukkit.configuration.Configuration defaults) {
        wrapped.addDefaults(defaults);
    }

    @Override
    public void load(File file) throws IOException, InvalidConfigurationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load(String file) throws IOException, InvalidConfigurationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load(Reader reader) throws IOException, InvalidConfigurationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(File file) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void save(String file) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConfigurationSection createSection(String path) {
        return new WrappedConfigurationSection(wrapped.createSection(path), this);
    }

    @Override
    public ConfigurationSection createSection(String path, Map<?, ?> map) {
        return new WrappedConfigurationSection(wrapped.createSection(path, map), this);
    }
}
