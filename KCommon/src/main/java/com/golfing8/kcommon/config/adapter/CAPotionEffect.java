package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts instances of {@link CAPotionEffect}
 */
public class CAPotionEffect implements ConfigAdapter<PotionEffect> {
    @Override
    public Class<PotionEffect> getAdaptType() {
        return PotionEffect.class;
    }

    @Override
    public PotionEffect toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        if (entry.getPrimitive() instanceof String) {
            String[] data = entry.getPrimitive().toString().split(":");
            PotionEffectType effectType = PotionEffectType.getByName(data[0]);
            // I'm not sure the actual version this changed, this is merely a guess.
            int infiniteDuration = KCommon.getInstance().getServerVersion().isAtOrAfter(NMSVersion.v1_17) ? -1 : 999999999;
            int duration = data.length > 2 ? Integer.parseInt(data[2]) : infiniteDuration;
            int amplifier = data.length > 1 ? Integer.parseInt(data[1]) : 0;

            return new PotionEffect(effectType, duration, amplifier);
        }

        Map<String, Object> map = entry.unwrap();
        PotionEffectType effectType = PotionEffectType.getByName(map.get("effect-type").toString());

        int duration = (int) map.getOrDefault("duration", 600);
        int amplifier = (int) map.getOrDefault("amplifier", 0);

        boolean ambient = (boolean) map.getOrDefault("ambient", true);
        boolean particles = (boolean) map.getOrDefault("particles", true);
        return new PotionEffect(effectType, duration, amplifier, ambient, particles);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull PotionEffect object) {
        // Ambient with particles is default. Serialize as a string.
        if (object.isAmbient() && object.hasParticles()) {
            if (object.getDuration() > 999999999 || object.getDuration() < 0) {
                return ConfigPrimitive.ofString(String.join(":",
                        object.getType().getName(),
                        String.valueOf(object.getAmplifier())));
            }
            return ConfigPrimitive.ofString(
                    String.join(":",
                            object.getType().getName(),
                            String.valueOf(object.getAmplifier()),
                            String.valueOf(object.getDuration()))
            );
        }

        Map<String, Object> map = new HashMap<>();
        map.put("effect-type", object.getType().getName());

        map.put("duration", object.getDuration());
        map.put("amplifier", object.getAmplifier());

        map.put("ambient", object.isAmbient());
        map.put("particles", object.hasParticles());
        return ConfigPrimitive.ofMap(map);
    }
}
