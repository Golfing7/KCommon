package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.yaml.YamlConfig;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A YAML file of messages. Missing messages are written with their defaults.
 */
public class LangConfig {
    private static final Pattern KEY_PATTERN = Pattern.compile("[a-z\\-.]+", Pattern.CASE_INSENSITIVE);

    private final Map<String, Message> loadedMessages = new HashMap<>();
    @Getter
    private final Path configPath;
    private YamlConfig configuration;
    private boolean pendingSave;

    public LangConfig(Path configPath) {
        this.configPath = configPath;
    }

    /**
     * Loads the lang file, creating it if needed.
     */
    public void load() {
        this.loadedMessages.clear();
        try {
            if (!Files.exists(this.configPath)) {
                Files.createDirectories(this.configPath.getParent());
                Files.createFile(this.configPath);
            }
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to load language config under path %s!", this.configPath), exc);
        }
        this.configuration = new YamlConfig(this.configPath);
        this.configuration.load();
    }

    /**
     * Saves the lang file if a message was added since the last save.
     */
    public void save() {
        if (!pendingSave)
            return;

        this.configuration.save();
        this.pendingSave = false;
    }

    /**
     * Adds the lines under the key if the lang file doesn't have it yet.
     *
     * @param key   the key.
     * @param value the default lines.
     * @return true if the default was written to the file.
     */
    public boolean addLanguageConstant(String key, String... value) {
        return this.addLanguageConstant(key, Arrays.asList(value));
    }

    /**
     * Adds the lines under the key if the lang file doesn't have it yet.
     *
     * @param key   the key.
     * @param value the default lines.
     * @return true if the default was written to the file.
     */
    public boolean addLanguageConstant(String key, List<String> value) {
        return addLanguageConstant(key, new Message(value));
    }

    /**
     * Adds the message under the key if the lang file doesn't have it yet, otherwise loads the file's message.
     *
     * @param key   the key.
     * @param value the default message.
     * @return true if the default was written to the file.
     */
    public boolean addLanguageConstant(String key, Message value) {
        if (!KEY_PATTERN.matcher(key).matches())
            throw new IllegalArgumentException(String.format("Key %s does not match expected 'key-string-format' format!", key));

        if (!this.configuration.contains(key)) {
            ConfigTypeRegistry.setInConfig(this.configuration, key, value);
            this.loadedMessages.put(key, value);
            this.pendingSave = true;
            return true;
        }

        this.loadedMessages.put(key, new Message(configuration.get(key)));
        return false;
    }

    /**
     * Gets the loaded message under the key.
     *
     * @param key the key.
     * @return the message, or null if it was never added.
     */
    public Message getMessage(String key) {
        return this.loadedMessages.get(key);
    }

    /**
     * Gets the file name of the lang file.
     *
     * @return the name.
     */
    public String getConfigName() {
        return this.configuration.getName();
    }
}
