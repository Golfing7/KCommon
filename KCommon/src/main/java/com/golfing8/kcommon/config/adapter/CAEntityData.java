package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.EntityData;
import com.golfing8.kcommon.struct.reflection.FieldType;

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
    public ConfigPrimitive toPrimitive(EntityData object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        return ConfigPrimitive.ofString(object.toString());
    }
}
