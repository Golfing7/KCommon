package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class CAPotionEffect implements ConfigAdapter<PotionEffect> {
    @Override
    public Class<PotionEffect> getAdaptType() {
        return PotionEffect.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PotionEffect toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        if (entry.getPrimitive() instanceof String) {
            String[] data = entry.getPrimitive().toString().split(":");
            PotionEffectType effectType = PotionEffectType.getByName(data[0]);
            int duration = data.length > 2 ? Integer.parseInt(data[2]) : 999999999;
            int amplifier = data.length > 1 ? Integer.parseInt(data[1]) : 0;

            return new PotionEffect(effectType, duration, amplifier);
        }

        Map<String, Object> map = (Map<String, Object>) entry.unwrap();
        PotionEffectType effectType = PotionEffectType.getByName(map.get("effect-type").toString());

        int duration = (int) map.getOrDefault("duration", 600);
        int amplifier = (int) map.getOrDefault("amplifier", 0);

        boolean ambient = (boolean) map.getOrDefault("ambient", false);
        boolean particles = (boolean) map.getOrDefault("particles", false);
        return new PotionEffect(effectType, duration, amplifier, ambient, particles);
    }

    @Override
    public ConfigPrimitive toPrimitive(PotionEffect object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        Map<String, Object> map = new HashMap<>();
        map.put("effect-type", object.getType().getName());

        map.put("duration", object.getDuration());
        map.put("amplifier", object.getAmplifier());

        map.put("ambient", object.isAmbient());
        map.put("particles", object.hasParticles());
        return ConfigPrimitive.ofMap(map);
    }
}
