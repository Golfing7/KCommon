package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.Reflection;
import com.golfing8.kcommon.util.StringUtil;
import lombok.var;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * This config adapter reflectively serializes and deserializes the incoming object.
 */
public class CAReflective implements ConfigAdapter<CASerializable> {
    private static final String KEY_FIELD_NAME = "_key";

    private final Map<Class<?>, Map<String, FieldHandle<?>>> typeFieldCache = new HashMap<>();
    private final Map<Class<?>, Constructor<?>> constructorCache = new HashMap<>();

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
                Reflection.getAllFieldHandles(type.getType());

        CASerializable instance;
        try {
            Constructor<?> constructor = null;
            if (constructorCache.containsKey(type.getType())) {
                constructor = constructorCache.get(type.getType());
            } else {
                constructor = type.getType().getDeclaredConstructor();
                constructor.setAccessible(true);
            }
            instance = (CASerializable) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            KCommon.getInstance().getLogger().severe(String.format("Failed to deserialize type %s!", type.getType().getName()));
            throw new RuntimeException(e);
        }
        for (var fieldEntry : fieldHandles.entrySet()) {
            var handle = fieldEntry.getValue();
            if (!handle.shouldSerialize())
                continue;

            // Don't serialize the field name.
            if (handle.getField().getName().equals(KEY_FIELD_NAME))
                continue;

            String key = StringUtil.camelToYaml(fieldEntry.getKey());
            // Don't override default values if the value is not present at all.
            if (!primitives.containsKey(key))
                continue;

            Object primitiveValue = primitives.get(key);
            if (primitiveValue == null) {
                handle.set(instance, null);
                continue;
            }

            var fieldType = new FieldType(handle.getField());
            Object deserialized = ConfigTypeRegistry.getFromType(entry.getSubValue(key), fieldType);
            handle.set(instance, deserialized);
        }
        // Set the key field name if necessary.
        if (fieldHandles.containsKey(KEY_FIELD_NAME) && entry.getSource() != null && entry.getSource().getName() != null) {
            fieldHandles.get(KEY_FIELD_NAME).set(instance, entry.getSource().getName());
        }
        return instance;
    }

    @Override
    public ConfigPrimitive toPrimitive(CASerializable object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        var fieldHandles = typeFieldCache.containsKey(object.getClass()) ?
                typeFieldCache.get(object.getClass()) :
                Reflection.getAllFieldHandles(object.getClass());

        // Load and serialize all fields...
        Map<String, Object> primitives = new HashMap<>();

        for (var fieldEntry : fieldHandles.entrySet()) {
            var handle = fieldEntry.getValue();
            if (!handle.shouldSerialize())
                continue;

            // Don't serialize the field name.
            if (handle.getField().getName().equals(KEY_FIELD_NAME))
                continue;

            ConfigPrimitive primitiveValue = ConfigTypeRegistry.toPrimitive(handle.get(object));
            primitives.put(StringUtil.camelToYaml(fieldEntry.getKey()), primitiveValue.unwrap());
        }
        return ConfigPrimitive.ofMap(primitives);
    }
}
