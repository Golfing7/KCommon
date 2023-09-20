package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.potion.PotionEffectType;

/**
 * As the {@link PotionEffectType} class isn't an enum, special serialization is necessary.
 */
public class CAPotionEffectType implements ConfigAdapter<PotionEffectType> {
    @Override
    public Class<PotionEffectType> getAdaptType() {
        return PotionEffectType.class;
    }

    @Override
    public PotionEffectType toPOJO(ConfigPrimitive entry, FieldType type) {
        return PotionEffectType.getByName(entry.getPrimitive().toString());
    }

    @Override
    public ConfigPrimitive toPrimitive(PotionEffectType object) {
        return ConfigPrimitive.ofString(object.getName());
    }
}
