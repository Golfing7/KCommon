package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.generator.ConfigValueHandle;
import lombok.Getter;

/**
 * Represents an entry in a config.
 */
@Getter
public class MappedConfigEntry extends ConfigEntry {
    /**
     * The handle this entry is mapped to
     */
    private final ConfigValueHandle handle;

    public MappedConfigEntry(KSection section, String key, ConfigValueHandle handle) {
        super(section, key);
        this.handle = handle;
    }
}
