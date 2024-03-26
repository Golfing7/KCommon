package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.Color;

/**
 * Adapts bukkit colors.
 */
public class CAColorBukkit implements ConfigAdapter<Color> {
    @Override
    public Class<Color> getAdaptType() {
        return Color.class;
    }

    @Override
    public Color toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Object value = entry.unwrap();
        Color color;
        if (value instanceof String) {
            color = Color.fromRGB(Integer.parseInt(value.toString(), 16));
        } else if (value instanceof Integer) {
            color = Color.fromRGB((Integer) value);
        } else {
            throw new RuntimeException("Unknown object for color " + value);
        }
        return color;
    }

    @Override
    public ConfigPrimitive toPrimitive(Color object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        return ConfigPrimitive.ofString(Integer.toString(object.asRGB(), 16));
    }
}
