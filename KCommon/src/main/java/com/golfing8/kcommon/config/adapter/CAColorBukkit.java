package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapts instances of {@link org.bukkit.Color}
 */
public class CAColorBukkit implements ConfigAdapter<Color> {
    /**
     * Caches colors
     */
    private static final Map<String, Color> colorCache = new HashMap<>();

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
        return ConfigPrimitive.ofString(String.format("%06X", object.asRGB() & 0xFFFFFF));
    }

    private static Color getColorByName(String name) {
        if (colorCache.containsKey(name))
            return colorCache.get(name);

        FieldHandle<Color> fieldHandle = FieldHandles.getHandle(name, Color.class);
        Color color = fieldHandle.get(null);
        if (color != null)
            colorCache.put(name, color);
        return color;
    }
}
