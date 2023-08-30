package com.golfing8.kcommon.config.generator;

import com.golfing8.kcommon.module.Module;

/**
 * A wrapper implementation of {@link ConfigClass}. Used primarily for {@link Module} instances.
 */
public class ConfigClassWrapper extends ConfigClass {
    public ConfigClassWrapper(ConfigClass parent, Class<?> delegate, Object instance) {
        super(parent, delegate, instance);
    }
}
