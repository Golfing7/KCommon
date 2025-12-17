package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adapts instances of {@link EntityAttribute}
 */
public class CAEntityAttribute implements ConfigAdapter<EntityAttribute> {
    @Override
    public Class<EntityAttribute> getAdaptType() {
        return EntityAttribute.class;
    }

    @Override
    public @Nullable EntityAttribute toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return EntityAttribute.byName(entry.getPrimitive().toString());
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull EntityAttribute object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
