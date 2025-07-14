package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents an entry in a config.
 */
@Getter
@AllArgsConstructor
public class ConfigEntry {
    /**
     * An ancestor section of the entry
     */
    private final ConfigurationSection section;
    /**
     * The key to the value of the entry
     */
    private final String key;

    /**
     * Gets the value.
     *
     * @return the value.
     */
    public Object get() {
        return section.get(key);
    }

    /**
     * Gets a config primitive of this value.
     *
     * @return the primitive value.
     */
    public ConfigPrimitive getPrimitive() {
        return ConfigPrimitive.ofValue(this);
    }
}
