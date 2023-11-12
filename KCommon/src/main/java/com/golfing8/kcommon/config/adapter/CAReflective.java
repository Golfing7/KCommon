package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.Reflection;
import lombok.var;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * This config adapter reflectively serializes and deserializes the incoming object.
 */
public class CAReflective implements ConfigAdapter<CASerializable> {
    private final Map<Class<?>, Map<String, FieldHandle<?>>> typeFieldCache = new HashMap<>();

    @Override
    public Class<CASerializable> getAdaptType() {
        return CASerializable.class;
    }

    @Override
    public CASerializable toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> primitives = entry.unwrap();

        var fieldHandles = typeFieldCache.containsKey(type.getType()) ?
                typeFieldCache.get(type.getType()) :
                Reflection.getAllFields(type.getType());

        CASerializable instance;
        try {
            instance = (CASerializable) type.getType().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            KCommon.getInstance().getLogger().severe(String.format("Failed to deserialize type %s!", type.getType().getName()));
            throw new RuntimeException(e);
        }
        for (var fieldEntry : fieldHandles.entrySet()) {
            var handle = fieldEntry.getValue();
            Object primitiveValue = primitives.get(fieldEntry.getKey());
            if (primitiveValue == null) {
                handle.set(instance, null);
                continue;
            }

            var fieldType = new FieldType(handle.getField());
            Object deserialized = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofTrusted(primitiveValue), fieldType);
            handle.set(instance, deserialized);
        }
        return instance;
    }

    @Override
    public ConfigPrimitive toPrimitive(CASerializable object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        var fieldHandles = typeFieldCache.containsKey(object.getClass()) ?
                typeFieldCache.get(object.getClass()) :
                Reflection.getAllFields(object.getClass());

        // Load and serialize all fields...
        Map<String, Object> primitives = new HashMap<>();

        for (var fieldEntry : fieldHandles.entrySet()) {
            var handle = fieldEntry.getValue();

            ConfigPrimitive primitiveValue = ConfigTypeRegistry.toPrimitive(handle.get(object));
            primitives.put(fieldEntry.getKey(), primitiveValue.unwrap());
        }
        return ConfigPrimitive.ofMap(primitives);
    }
}
