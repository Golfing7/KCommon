package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.adapter.CAMessage;
import com.golfing8.kcommon.config.adapter.CARegion;
import com.golfing8.kcommon.config.adapter.ConfigAdapter;

import java.util.Arrays;
import java.util.List;

/**
 * The config adapters for Hytale types, found by {@link ConfigTypeRegistry} through {@link java.util.ServiceLoader}.
 */
public class HytaleConfigAdapters implements ConfigAdapterSource {
    @Override
    public List<ConfigAdapter<?>> getAdapters() {
        return Arrays.asList(
                new CAMessage(),
                new CARegion()
        );
    }
}
