package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.adapter.ConfigAdapter;

import java.util.List;

/**
 * Supplies the config adapters a platform adds on top of the shared ones.
 * <p>
 * Implementations are found with {@link java.util.ServiceLoader} when {@link ConfigTypeRegistry} first loads.
 * </p>
 */
public interface ConfigAdapterSource {
    /**
     * Gets the adapters to register.
     *
     * @return the adapters.
     */
    List<ConfigAdapter<?>> getAdapters();
}
