package com.golfing8.kcommon.config.exc;

import com.golfing8.kcommon.config.KFileConfig;
import com.golfing8.kcommon.config.KSection;
import org.jetbrains.annotations.Nullable;

/**
 * A type of exception thrown that identifies a config as the source of the problem
 */
public class ConfigException extends RuntimeException {
    private final @Nullable KSection config;

    public ConfigException(@Nullable KSection config, String message) {
        super(formatConfigPrefix(config) + message);
        this.config = config;
    }

    public ConfigException(@Nullable KSection config, String message, Throwable cause) {
        super(formatConfigPrefix(config) + message, cause);
        this.config = config;
    }

    private static String formatConfigPrefix(@Nullable KSection section) {
        if (section == null)
            return "";

        if (section instanceof KFileConfig) {
            KFileConfig config = (KFileConfig) section;
            return config.getModule() != null ? config.getModule().getModuleName() + "_" + config.getName() + ": " : config.getName() + ": ";
        }
        return section.getCurrentPath();
    }
}
