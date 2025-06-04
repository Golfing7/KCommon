package com.golfing8.kcommon.config.exc;

import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;

/**
 * Thrown when something in a config has been malformed or isn't defined properly.
 */
public class ImproperlyConfiguredValueException extends ConfigException {
    private static final String FORMAT_NO_EXPECTING = "In file '%s' under path '%s' config option '%s' wasn't defined!";
    private static final String FORMAT_EXPECTING = "In file '%s' under path '%s' config option '%s' wasn't defined! Expecting '%s'!";

    public ImproperlyConfiguredValueException(@Nullable ConfigurationSection section, String configOption) {
        super(section, String.format(FORMAT_NO_EXPECTING, "unknown", section == null ? "unknown path" : section.getCurrentPath(), configOption));
    }

    public ImproperlyConfiguredValueException(@Nullable ConfigurationSection section, String configOption, String expecting) {
        super(section, String.format(FORMAT_EXPECTING, "unknown", section == null ? "unknown path" : section.getCurrentPath(), configOption, expecting));
    }
}
