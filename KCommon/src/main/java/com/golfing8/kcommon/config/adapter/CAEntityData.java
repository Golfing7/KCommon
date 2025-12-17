package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.nms.struct.EntityData;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * Adapts instances of {@link EntityData}
 */
public class CAEntityData implements ConfigAdapter<EntityData> {
    @Override
    public Class<EntityData> getAdaptType() {
        return EntityData.class;
    }

    @Override
    public EntityData toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        String data = (String) entry.getPrimitive();
        return EntityData.valueOf(data);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull EntityData object) {
        return ConfigPrimitive.ofString(object.toString());
    }
}
