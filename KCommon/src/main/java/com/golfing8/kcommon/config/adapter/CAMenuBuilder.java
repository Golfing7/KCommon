package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.menu.MenuBuilder;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.Nullable;

/**
 * Adapts instances of {@link MenuBuilder}
 */
public class CAMenuBuilder implements ReadOnlyConfigAdapter<MenuBuilder> {
    @Override
    public Class<MenuBuilder> getAdaptType() {
        return MenuBuilder.class;
    }

    @Override
    public @Nullable MenuBuilder toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null || entry.getSource() == null)
            return null;

        return new MenuBuilder(entry.getSource());
    }
}
