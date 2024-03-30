package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.nms.struct.PotionData;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A serializer for local {@link PotionData} instances.
 */
public class CAPotionData implements ConfigAdapter<PotionData> {
    @Override
    public Class<PotionData> getAdaptType() {
        return PotionData.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PotionData toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> values = (Map<String, Object>) entry.unwrap();
        PotionType potionType = PotionType.valueOf(values.get("potion-type").toString().toUpperCase());
        boolean amplified = (boolean) values.getOrDefault("amplified", false);
        boolean extended = (boolean) values.getOrDefault("extended", false);
        return new PotionData(potionType, extended, amplified);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull PotionData object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        Map<String, Object> serialized = new HashMap<>();
        serialized.put("potion-type", object.getPotionType().name());
        serialized.put("amplified", object.isAmplified());
        serialized.put("extended", object.isExtended());
        return ConfigPrimitive.ofMap(serialized);
    }
}
