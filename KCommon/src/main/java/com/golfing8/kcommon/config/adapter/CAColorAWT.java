package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * Adapts instances of {@link java.awt.Color}
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
            try {
                int tryValue = Integer.parseInt((String) value, 16);
                color = new Color(tryValue);
            } catch (NumberFormatException exc) {
                Color colorByName = getColorByName(value.toString().toUpperCase());
                color = colorByName == null ? Color.WHITE : colorByName;
            }
        } else if (value instanceof Integer) {
            color = new Color((Integer) value);
        } else {
            throw new RuntimeException("Unknown object for color " + value);
        }
        return color;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Color object) {
        return ConfigPrimitive.ofString(String.format("%06X", object.getRGB() & 0xFFFFFF));
    }

    private static Color getColorByName(String name) {
        FieldHandle<Color> fieldHandle = FieldHandles.getHandle(name, Color.class);
        return fieldHandle.get(null);
    }
}
