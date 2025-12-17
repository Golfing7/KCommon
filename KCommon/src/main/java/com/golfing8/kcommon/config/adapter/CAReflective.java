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
import java.util.Map;
import java.util.Objects;

/**
 * Adapts instances of {@link CASerializable}
 * <p>
 * This adapter allows for quick and easy serialization of basic POJOs.
 * The adapter will map a class' fields and load them to/from the config reflectively.
 * </p>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CAReflective implements ConfigAdapter<CASerializable> {
    private static final String TYPE_FIELD_NAME = "type";
    private static final String KEY_FIELD_NAME = "_key";

    private final Map<Class<?>, Map<String, FieldHandle<?>>> typeFieldCache = new HashMap<>();
    private final Map<Class<?>, Constructor<?>> constructorCache = new HashMap<>();
    private final Map<Class<?>, CASerializable.TypeResolver> typeResolverCache = new HashMap<>();

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

        // This handles polymorphism.
        Class<? extends CASerializable.TypeResolver> typeResolverClass = options != null && options.typeResolverEnum() != CASerializable.TypeResolver.class ?
                options.typeResolverEnum() :
                null;

        CASerializable.TypeResolver typeResolver = typeResolverClass != null ?
                (CASerializable.TypeResolver) Enum.valueOf((Class) typeResolverClass, primitives.get(TYPE_FIELD_NAME).toString().toUpperCase()) :
                null;

        Class<?> parentClass = getParentSerializableClass(type.getType());
        Class<?> deserializationType = typeResolver != null ? typeResolver.getType() : type.getType();
        var fieldHandles = typeFieldCache.containsKey(deserializationType) ?
                typeFieldCache.get(deserializationType) :
                Reflection.getAllFieldHandlesUpToIncluding(deserializationType, parentClass);

        CASerializable instance;
        try {
            Constructor<?> constructor;
            if (constructorCache.containsKey(deserializationType)) {
                constructor = constructorCache.get(deserializationType);
            } else {
                constructor = deserializationType.getDeclaredConstructor();
                constructor.setAccessible(true);
            }
            instance = (CASerializable) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            KCommon.getInstance().getLogger().severe(String.format("Failed to deserialize type %s!", deserializationType.getName()));
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
        Class<?> parentClass = getParentSerializableClass(object.getClass());
        CASerializable.Options options = parentClass.getAnnotation(CASerializable.Options.class);
        boolean flatten = options != null && options.flatten();
        var fieldHandles = typeFieldCache.containsKey(object.getClass()) ?
                typeFieldCache.get(object.getClass()) :
                Reflection.getAllFieldHandlesUpToIncluding(object.getClass(), parentClass);

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
        // Check if we need to serialize type.
        if (options != null && options.typeResolverEnum() != CASerializable.TypeResolver.class) {
            CASerializable.TypeResolver foundType = typeResolverCache.get(object.getClass());
            if (foundType != null) {
                primitives.put(TYPE_FIELD_NAME, foundType.toString());
            } else {
                for (CASerializable.TypeResolver resolver : options.typeResolverEnum().getEnumConstants()) {
                    typeResolverCache.put(resolver.getType(), resolver);
                }
                primitives.put(TYPE_FIELD_NAME, Objects.requireNonNull(typeResolverCache.get(object.getClass()), "Type resolver is null. Is a type missing from " + options.typeResolverEnum().getName() + "?"));
            }

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

    private static Class<?> getParentSerializableClass(Class<?> clazz) {
        if (!CASerializable.class.isAssignableFrom(clazz))
            return null;

        Class<?> current = clazz;
        while (current != Object.class) {
            Class<?> superClass = current.getSuperclass();
            if (superClass == null || !CASerializable.class.isAssignableFrom(superClass)) {
                // Now, find by interface.
                for (Class<?> interfaceClass : current.getInterfaces()) {
                    if (interfaceClass == CASerializable.class)
                        return current;

                    Class<?> found = getParentSerializableClass(interfaceClass);
                    if (found != null)
                        return found;
                }
                return current;
            }
            current = clazz.getSuperclass();
        }
        return null;
    }
}
