package com.golfing8.kcommon.config.exc;

import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.commented.MConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

/**
 * A type of exception thrown that identifies a config as the source of the problem
 */
public class ConfigException extends RuntimeException {
    private final @Nullable ConfigurationSection config;

    public ConfigException(@Nullable ConfigurationSection config, String message) {
        super(formatConfigPrefix(config) + message);
        this.config = config;
    }

    public ConfigException(@Nullable ConfigurationSection config, String message, Throwable cause) {
        super(formatConfigPrefix(config) + message, cause);
        this.config = config;
    }

    private static String formatConfigPrefix(@Nullable ConfigurationSection section) {
        if (section == null)
            return "";

        if (section instanceof MConfiguration) {
            MConfiguration mc = (MConfiguration) section;
            return mc.getModule().getModuleName() + "_" + mc.getName() + ": ";
        } else if (section instanceof Configuration) {
            Configuration conf = (Configuration) section;
            return conf.getName() + ": ";
        }
        return section.getCurrentPath();
    }
}
