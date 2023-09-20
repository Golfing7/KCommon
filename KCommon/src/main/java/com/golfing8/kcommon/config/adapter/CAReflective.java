package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.Reflection;

/**
 * This config adapter reflectively serializes and deserializes the incoming object.
 */
public class CAReflective implements ConfigAdapter<Object> {
    @Override
    public Class<Object> getAdaptType() {
        return Object.class;
    }

    @Override
    public Object toPOJO(ConfigPrimitive entry, FieldType type) {
        return null;
    }

    @Override
    public ConfigPrimitive toPrimitive(Object object) {
        return null;
    }
}
