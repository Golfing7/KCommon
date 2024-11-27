package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.Reflection;
import com.golfing8.kcommon.util.StringUtil;
import lombok.var;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

        // Get the options of the serializable.
        CASerializable.Options options = type.getType().getAnnotation(CASerializable.Options.class);
        if (options != null && options.canDelegate() && entry.getPrimitive() instanceof String && entry.getSource() != null) {
            String delegatePath = (String) entry.getPrimitive();
            ConfigurationSection root = entry.getSource().getRoot();
            // If we don't have the delegate, we can't do anything.
            if (!root.contains(delegatePath)) {
                throw new IllegalStateException("Cannot load delegate path " + delegatePath + " under key " + entry.getSource().getCurrentPath() + " as it doesn't exist!");
            }

            return ConfigTypeRegistry.getFromType(new ConfigEntry(root, delegatePath), type);
        }

        Map<String, Object> primitives = entry.unwrap();

        Class<?> maxParentClass = options != null && options.serializeUpTo() != Object.class ? options.serializeUpTo() : Object.class;
        var fieldHandles = typeFieldCache.containsKey(type.getType()) ?
                typeFieldCache.get(type.getType()) :
                Reflection.getAllFieldHandlesUpToIncluding(type.getType(), maxParentClass);

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

        // Check if we should try flattening the data.
        boolean flatten = options != null && options.flatten();

        Map<String, FieldHandle<?>> serializableFields = new HashMap<>();
        for (var fieldEntry : fieldHandles.entrySet()) {
            var handle = fieldEntry.getValue();
            if (!handle.shouldSerialize())
                continue;

            // Don't serialize the field name.
            if (handle.getField().getName().equals(KEY_FIELD_NAME))
                continue;

            serializableFields.put(fieldEntry.getKey(), fieldEntry.getValue());
        }

        for (var fieldEntry : serializableFields.entrySet()) {
            var handle = fieldEntry.getValue();
            if (flatten && serializableFields.size() == 1) {
                var fieldType = new FieldType(handle.getField());
                Object deserialized = ConfigTypeRegistry.getFromType(entry, fieldType);
                handle.set(instance, deserialized);
                continue;
            }

            // Don't override default values if the value is not present at all.
            String key = StringUtil.camelToYaml(fieldEntry.getKey());
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
        instance.onDeserialize();
        instance.onDeserialize(entry);
        return instance;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull CASerializable object) {
        CASerializable.Options options = object.getClass().getAnnotation(CASerializable.Options.class);
        boolean flatten = options != null && options.flatten();
        Class<?> maxParentClass = options != null && options.serializeUpTo() != Object.class ? options.serializeUpTo() : Object.class;
        var fieldHandles = typeFieldCache.containsKey(object.getClass()) ?
                typeFieldCache.get(object.getClass()) :
                Reflection.getAllFieldHandlesUpToIncluding(object.getClass(), maxParentClass);

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
        object.onSerialize();

        // Try to do flattening if possible
        if (flatten && primitives.size() <= 1) {
            if (primitives.isEmpty())
                return ConfigPrimitive.ofMap(Collections.emptyMap());

            return ConfigPrimitive.of(primitives.values().stream().findFirst().get());
        }
        return ConfigPrimitive.ofMap(primitives);
    }
}
