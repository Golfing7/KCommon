package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

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
            try {
                int tryValue = Integer.parseInt(value.toString(), 16);
                color = Color.fromRGB(tryValue);
            } catch (NumberFormatException exc) {
                Color colorByName = getColorByName(value.toString().toUpperCase());
                color = colorByName == null ? Color.WHITE : colorByName;
            }
        } else if (value instanceof Integer) {
            color = Color.fromRGB((Integer) value);
        } else {
            throw new RuntimeException("Unknown object for color " + value);
        }
        return color;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Color object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        return ConfigPrimitive.ofString(Integer.toString(object.asRGB(), 16));
    }

    @SuppressWarnings("unchecked")
    private static Color getColorByName(String name) {
        FieldHandle<Color> fieldHandle = (FieldHandle<Color>) FieldHandles.getHandle(name, Color.class);
        return fieldHandle.get(null);
    }
}
