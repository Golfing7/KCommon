package com.golfing8.kcommon.config;

import com.golfing8.kcommon.codec.HytaleCodecs;
import com.golfing8.kcommon.config.adapter.CAHytaleCodec;
import com.golfing8.kcommon.config.adapter.CAMessage;
import com.golfing8.kcommon.config.adapter.CARegion;
import com.golfing8.kcommon.config.adapter.ConfigAdapter;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.inventory.ItemStack;

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
                new CARegion(),
                new CAHytaleCodec<>(ItemStack.class, HytaleCodecs.find(ItemStack.class)),
                new CAHytaleCodec<>(Location.class, HytaleCodecs.LOCATION_CODEC),
                new CAHytaleCodec<>(Box.class, Box.CODEC)
        );
    }
}
