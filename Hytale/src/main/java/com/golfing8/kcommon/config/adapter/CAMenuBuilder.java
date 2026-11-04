package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.menu.MenuBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Loads Hytale {@link MenuBuilder}s from config.
 */
public class CAMenuBuilder implements ReadOnlyConfigAdapter<MenuBuilder> {
    @Override
    public Class<MenuBuilder> getAdaptType() {
        return MenuBuilder.class;
    }

    @Override
    public @Nullable MenuBuilder toPOJO(ConfigPrimitive entry, FieldType type) {
        Object raw = entry.unwrap();
        if (!(raw instanceof Map))
            return null;

        @SuppressWarnings("unchecked")
        Map<String, Object> section = (Map<String, Object>) raw;
        String path = entry.getSource() == null ? "menu" : entry.getSource().getCurrentPath();
        return new MenuBuilder(section, path);
    }
}
