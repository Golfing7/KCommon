package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.adapter.*;
import com.golfing8.kcommon.config.adapter.xseries.*;

import java.util.Arrays;
import java.util.List;

/**
 * The config adapters for Bukkit types, found by {@link ConfigTypeRegistry} through {@link java.util.ServiceLoader}.
 */
public class BukkitConfigAdapters implements ConfigAdapterSource {
    @Override
    public List<ConfigAdapter<?>> getAdapters() {
        return Arrays.asList(
                new CARegion(),
                new CAItemStackBuilder(),
                new CALocation(),
                new CABlockVector(),
                new CAMessage(),
                new CASoundWrapper(),
                new CATitle(),
                new CAItemFilter(),
                new CAPotionEffectType(),
                new CAPotionData(),
                new CAPotionEffect(),
                new CAWorld(),
                new CAEntityData(),
                new CAVector(),
                new CAColorBukkit(),
                new CADrop(),
                new CAEntityAttribute(),
                new CAEntityAttributeModifier(),
                new CAMenuCoordinate(),
                new CAParticle(),
                new CAMenuBuilder(),
                new CAMoveLength(),
                new CALayoutShape(),
                new CAXBiome(),
                new CAXEnchantment(),
                new CAXEntityType(),
                new CAXMaterial(),
                new CAXPotion(),
                new CAXSound(),
                new CACraftingRecipe(),
                new CAComponent(),
                new CACurrency(),
                new CABookData()
        );
    }
}
