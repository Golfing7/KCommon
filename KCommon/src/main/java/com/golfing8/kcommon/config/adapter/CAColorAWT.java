package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Adapts java AWT colors.
 */
public class CAColorAWT implements ConfigAdapter<Color> {
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
            color = new Color(Integer.parseInt(value.toString(), 16));
        } else if (value instanceof Integer) {
            color = new Color((Integer) value);
        } else {
            throw new RuntimeException("Unknown object for color " + value);
        }
        return color;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Color object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        return ConfigPrimitive.ofString(Integer.toString(object.getRGB(), 16));
    }
}
