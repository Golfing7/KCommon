package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ImproperlyConfiguredValueException;
import com.golfing8.kcommon.menu.MenuUtils;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A type adapter for menu coordinates.
 */
public class CAMenuCoordinate implements ConfigAdapter<MenuCoordinate> {
    @Override
    public Class<MenuCoordinate> getAdaptType() {
        return MenuCoordinate.class;
    }

    @Override
    public @Nullable MenuCoordinate toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        if (entry.getPrimitive() instanceof Number) {
            return new MenuCoordinate(((Number) entry.getPrimitive()).intValue());
        }

        Map<String, Object> section = entry.unwrap();
        int x = (int) section.get("x");
        int y = (int) section.get("y");
        return new MenuCoordinate(x, y);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull MenuCoordinate object) {
        return ConfigPrimitive.ofInt((object.getY() - 1) * 9 + object.getX());
    }
}
