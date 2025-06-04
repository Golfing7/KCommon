package com.golfing8.kcommon.config.exc;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public class ConfigException extends RuntimeException {
    private final @Nullable ConfigurationSection config;
    public ConfigException(@Nullable ConfigurationSection config, String message) {
        super(message);
        this.config = config;
    }

    public ConfigException(@Nullable ConfigurationSection config, String message, Throwable cause) {
        super(message, cause);
        this.config = config;
    }
}
