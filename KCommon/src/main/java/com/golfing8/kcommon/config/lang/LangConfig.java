package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Represents a language config. Can be used in the {@link com.golfing8.kcommon.module.Module} class
 * for storing language constants for that module.
 */
public class LangConfig {
    private static final Pattern KEY_PATTERN = Pattern.compile(
            "[a-z\\-.]+"
    );

    /**
     * The loaded messages from the language config.
     */
    private final Map<String, Message> loadedMessages;
    /**
     * The path of the config.
     */
    private final Path configPath;
    /**
     * The configuration this lang config is backed by.
     */
    private YamlConfiguration configuration;
    /**
     * Signifies that a save is necessary, usually happens when a default message is added that didn't
     * previously exist in the config.
     */
    private boolean pendingSave;

    /**
     * Creates a lang config and loads it from a file.
     *
     * @param configPath the path to the config.
     */
    public LangConfig(Path configPath) {
        this.loadedMessages = new HashMap<>();
        this.configPath = configPath;
    }

    /**
     * Loads this language config from the path.
     */
    public void load() {
        this.loadedMessages.clear();

        //Try to create the file first.
        if(!Files.exists(this.configPath)) {
            try{
                Files.createDirectories(this.configPath.getParent());
                Files.createFile(this.configPath);
            }catch(IOException exc) {
                throw new RuntimeException(String.format("Failed to load language config under path %s!", this.configPath), exc);
            }
        }

        try(BufferedReader reader = Files.newBufferedReader(this.configPath)) {
            this.configuration = YamlConfiguration.loadConfiguration(reader);
        }catch(IOException exc) {
            throw new RuntimeException(String.format("Failed to load language config under path %s!", this.configPath), exc);
        }
    }

    /**
     * Saves this config.
     */
    public void save() {
        //No pending save? Not necessary then.
        if(!pendingSave)
            return;

        try{
            this.configuration.save(this.configPath.toFile());
        }catch(IOException exc) {
            throw new RuntimeException(String.format("Failed to save language config under path %s!", this.configPath), exc);
        }

        this.pendingSave = false;
    }

    /**
     * Used to add a language constant to this config.
     *
     * @param key the key.
     * @param value the values.
     * @return true if the item was added to the config.
     */
    public boolean addLanguageConstant(String key, String... value) {
        return this.addLanguageConstant(key, Arrays.asList(value));
    }

    /**
     * Used to add a language constant to this config.
     *
     * @param key the key.
     * @param value the value.
     * @return true if the item was added to the config.
     */
    public boolean addLanguageConstant(String key, List<String> value) {
        return addLanguageConstant(key, new Message(value, null, null));
    }

    /**
     * Used to add a language constant to this config.
     *
     * @param key the key.
     * @param value the value.
     * @return true if the item was added to the config.
     */
    public boolean addLanguageConstant(String key, Message value) {
        if(!KEY_PATTERN.matcher(key).matches())
            throw new IllegalArgumentException(String.format("Key %s does not match expected 'key-string-format' format!", key));

        if(!this.configuration.contains(key)) {
            ConfigTypeRegistry.setInConfig(this.configuration, key, value);
            this.loadedMessages.put(key, value);
            this.pendingSave = true;
            return true;
        }else {
            //Load the item.
            this.loadedMessages.put(key, new Message(configuration.get(key)));
        }
        return false;
    }

    /**
     * Gets the message with the given key.
     *
     * @param key the key.
     * @return the message.
     */
    public Message getMessage(String key) {
        return this.loadedMessages.get(key);
    }

    /**
     * Gets the config name used to save this config.
     *
     * @return the config name.
     */
    public String getConfigName() {
        return this.configuration.getName();
    }

    /**
     * Gets the full path where this config file will be saved.
     *
     * @return the config path.
     */
    public Path getConfigPath() {
        return this.configPath;
    }
}
