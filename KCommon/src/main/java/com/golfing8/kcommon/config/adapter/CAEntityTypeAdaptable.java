package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.exc.ConfigException;
import com.golfing8.kcommon.struct.entity.EntityTypeAdaptable;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * Adapts instances of {@link EntityTypeAdaptable}
 */
public class CAEntityTypeAdaptable implements ConfigAdapter<EntityTypeAdaptable> {
    @Override
    public Class<EntityTypeAdaptable> getAdaptType() {
        return EntityTypeAdaptable.class;
    }

    @Override
    public EntityTypeAdaptable toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        String data = (String) entry.getPrimitive();
        return EntityTypeAdaptable.fromString(data).orElseThrow(() -> new ConfigException(entry.getSource(), "Entity type " + data + " not recognized"));
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull EntityTypeAdaptable object) {
        return ConfigPrimitive.ofString(object.toString());
    }
}
