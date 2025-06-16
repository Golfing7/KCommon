package com.golfing8.kcommon.config;

import com.golfing8.kcommon.config.adapter.*;
import com.golfing8.kcommon.config.adapter.xseries.*;
import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.commented.MConfiguration;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents common things for configurations across the plugin.
 */
public class ConfigTypeRegistry {
    private static final String DELEGATE_PREFIX = "!delegate!";
    /** A map containing special functions for translating certain things in configs. */
    private static final Map<Class<?>, Function<ConfigurationSection, ?>> CONFIG_TO_VALUE = new HashMap<>();

    /** A map containing a type to value transformer */
    private static final Map<Class<?>, BiConsumer<ConfigurationSection, ?>> VALUE_TO_CONFIG = new HashMap<>();
    /** The config adapters registered */
    private static final Map<Class<?>, ConfigAdapter<?>> CONFIG_ADAPTERS = new HashMap<>();
    /** A cache of class -> adapter. This contains subclasses of the adapter's implemented type. */
    private static final Map<Class<?>, ConfigAdapter<?>> ADAPTER_LOOKUP = new HashMap<>();

    /**
     * Tries to find an adapter for the given type.
     *
     * @param actualType the actual type.
     * @return the adapter.
     * @param <T> the class type.
     */
    @SuppressWarnings("unchecked")
    public static <T> ConfigAdapter<? super T> findAdapter(Class<T> actualType) {
        return (ConfigAdapter<? super T>) findAdapter((Type) actualType);
    }

    /**
     * Tries to find an adapter for the given type.
     *
     * @param actualType the actual type.
     * @return the adapter.
     */
    @SuppressWarnings({"rawtypes"})
    public static ConfigAdapter<?> findAdapter(Type actualType) {
        Class<?> clazzType = (actualType instanceof ParameterizedType) ? (Class<?>) ((ParameterizedType) actualType).getRawType() : (Class<?>) actualType;
        // Base case for recursive section.
        if (clazzType == Object.class || clazzType == null)
            return null;

        if (ADAPTER_LOOKUP.containsKey(clazzType))
            return ADAPTER_LOOKUP.get(clazzType);

        // Direct registration?
        if (CONFIG_ADAPTERS.containsKey(clazzType))
            return CONFIG_ADAPTERS.get(clazzType);

        // If not, check parents.
        ConfigAdapter parent = findAdapter(clazzType.getSuperclass());
        if (parent != null) {
            ADAPTER_LOOKUP.put(clazzType, parent);
            return parent;
        }

        // And if it's still not found, check interfaces.
        for (Class<?> iface : clazzType.getInterfaces()) {
            ConfigAdapter adapter = findAdapter(iface);
            if (adapter != null) {
                ADAPTER_LOOKUP.put(clazzType, adapter);
                return adapter;
            }
        }
        // Nothing found :(
        return null;
    }

    /**
     * Unregisters the given class' config adapter.
     *
     * @param clazz the class.
     */
    public static void unregisterAdapter(Class<?> clazz) {
        VALUE_TO_CONFIG.remove(clazz);
        CONFIG_TO_VALUE.remove(clazz);
    }

    /**
     * Registers an adapter for the given class type.
     *
     * @param clazz the class type.
     * @param toValue the config to value function.
     * @param toConfig the consumer for setting in the config.
     * @param <T> the type of the class
     */
    public static <T> void registerAdapter(Class<T> clazz, Function<ConfigurationSection, T> toValue, BiConsumer<ConfigurationSection, T> toConfig) {
        Preconditions.checkNotNull(toValue);
        Preconditions.checkNotNull(toConfig);

        VALUE_TO_CONFIG.put(clazz, toConfig);
        CONFIG_TO_VALUE.put(clazz, toValue);
    }

    /**
     * Registers the adapter for the config.
     *
     * @param adapter the adapter.
     */
    public static void registerAdapter(ConfigAdapter<?> adapter) {
        Preconditions.checkNotNull(adapter);

        CONFIG_ADAPTERS.put(adapter.getAdaptType(), adapter);
        ADAPTER_LOOKUP.clear();
    }

    /**
     * Interprets the given section as the given class type.
     *
     * @param section the section to get from.
     * @return the value
     * @param <T> the type of value
     */
    @SuppressWarnings("unchecked")
    public static <T> T interpretSection(ConfigurationSection section, Class<T> type) {
        Function<ConfigurationSection, ?> function = CONFIG_TO_VALUE.get(type);
        if (function == null) {
            return null;
        }

        return (T) function.apply(section);
    }

    /**
     * Gets a value from the config by using the CONFIG_ADAPTER_MAP. If the type is unrecognized, the section's {@link ConfigurationSection#get(String, Object) get} method is used.
     *
     * @param entry the config entry.
     * @param clazz the class type.
     * @return the value
     * @param <T> the type of value
     */
    public static <T> T getFromType(ConfigEntry entry, Class<T> clazz) {
        return getFromType(entry, new FieldType(clazz));
    }

    /**
     * Gets a value from the config by using the CONFIG_ADAPTER_MAP. If the type is unrecognized, the section's {@link ConfigurationSection#get(String, Object) get} method is used.
     *
     * @param entry the config entry.
     * @param field the field type.
     * @return the value
     * @param <T> the type of value
     */
    public static <T> T getFromType(ConfigEntry entry, Field field) {
        return getFromType(entry, new FieldType(field));
    }

    /**
     * Gets a value from the config by using the CONFIG_ADAPTER_MAP. If the type is unrecognized, the section's {@link ConfigurationSection#get(String, Object) get} method is used.
     *
     * @param entry the config entry.
     * @param field the field type.
     * @return the value
     * @param <T> the type of value
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T getFromType(ConfigEntry entry, FieldType field) {
        ConfigAdapter<? super T> adapter = (ConfigAdapter<? super T>) findAdapter(field.getType());
        if (adapter == null) {
            return (T) entry.get();
        }

        // Check if the primitive is delegated.
        ConfigPrimitive primitive = entry.getPrimitive();
        if (primitive.getPrimitive() instanceof String && entry.getSection() != null) {
            String path = (String) primitive.getPrimitive();
            if (path.startsWith(DELEGATE_PREFIX)) {
                return loadFromDelegate(entry.getSection(), path.substring(DELEGATE_PREFIX.length()), field);
            }
        }

        return (T) adapter.toPOJO(entry.getPrimitive(), field);
    }

    /**
     * Gets a value from the config by using the CONFIG_ADAPTER_MAP. If the type is unrecognized, the section's {@link ConfigurationSection#get(String, Object) get} method is used.
     *
     * @param entry the config entry.
     * @param clazz the field type.
     * @return the value
     * @param <T> the type of value
     */
    public static <T> T getFromType(ConfigPrimitive entry, Class<T> clazz) {
        return getFromType(entry, new FieldType(clazz));
    }

    /**
     * Gets a value from the config by using the CONFIG_ADAPTER_MAP. If the type is unrecognized, the section's {@link ConfigurationSection#get(String, Object) get} method is used.
     *
     * @param entry the config entry.
     * @param field the field type.
     * @return the value
     * @param <T> the type of value
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T getFromType(ConfigPrimitive entry, FieldType field) {
        ConfigAdapter<? super T> adapter = (ConfigAdapter<? super T>) findAdapter(field.getType());
        if (adapter == null) {
            return (T) entry.getPrimitive();
        }

        // Check if the primitive is delegated.
        if (entry.getPrimitive() instanceof String && entry.getSource() != null) {
            String path = (String) entry.getPrimitive();
            if (path.startsWith(DELEGATE_PREFIX)) {
                return loadFromDelegate(entry.getSource(), path.substring(DELEGATE_PREFIX.length()), field);
            }
        }

        return (T) adapter.toPOJO(entry, field);
    }

    /**
     * Converts the value to a primitive object.
     *
     * @param value the value.
     * @return the primitive object.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ConfigPrimitive toPrimitive(Object value) {
        if (value == null)
            return ConfigPrimitive.ofNull();

        ConfigAdapter adapter = findAdapter(value.getClass());
        if (adapter == null) {
            return ConfigPrimitive.of(value);
        }

        return adapter.toPrimitive(value);
    }

    /**
     * Sets the value at the given configuration section.
     *
     * @param section the section.
     * @param path the path of the value.
     * @param value the value.
     */
    public static void setInConfig(ConfigurationSection section, String path, Object value) {
        // Special cases for config sections
        if (value instanceof ConfigurationSection) {
            section.set(path, value);
            return;
        }
        section.set(path, toPrimitive(value).unwrap());
    }

    /**
     * Loads the value from the given config delegate.
     *
     * @param context the context section.
     * @param path the path given for delegation.
     * @param fieldType the field type.
     * @return the loaded value, or null if it's not in a config.
     * @param <T> the type.
     */
    private static <T> @Nullable T loadFromDelegate(ConfigurationSection context, String path, FieldType fieldType) {
        Configuration root = context.getRoot() instanceof Configuration ? (Configuration) context.getRoot() : null;
        Module module = root instanceof MConfiguration ? ((MConfiguration) root).getModule() : null;
        ConfigPath configPath = ConfigPath.parseWithContext(module, root, path);

        // Any options?
        List<ConfigEntry> enumerate = configPath.enumerate();
        if (enumerate.isEmpty())
            return null;

        return getFromType(enumerate.get(0), fieldType);
    }

    // Register some common types.
    static {
        registerAdapter(new CAEnum());
        registerAdapter(new CARegion());
        registerAdapter(new CAList());
        registerAdapter(new CAMap());
        registerAdapter(new CAItemStackBuilder());
        registerAdapter(new CAWeightedCollection());
        registerAdapter(new CALocation());
        registerAdapter(new CABlockVector());
        registerAdapter(new CASet());
        registerAdapter(new CAZonedDateTime());
        registerAdapter(new CAMessage());
        registerAdapter(new CASoundWrapper());
        registerAdapter(new CATitle());
        registerAdapter(new CAItemFilter());
        registerAdapter(new CAStringFilter());
        registerAdapter(new CAPotionEffectType());
        registerAdapter(new CAPotionData());
        registerAdapter(new CAPotionEffect());
        registerAdapter(new CARange());
        registerAdapter(new CASchedule());
        registerAdapter(new CAWorld());
        registerAdapter(new CAReflective());
        registerAdapter(new CAEntityData());
        registerAdapter(new CARangeMap());
        registerAdapter(new CAVector());
        registerAdapter(new CAColorBukkit());
        registerAdapter(new CAColorAWT());
        registerAdapter(new CADrop());
        registerAdapter(new CAEntityAttribute());
        registerAdapter(new CAEntityAttributeModifier());
        registerAdapter(new CAMenuCoordinate());
        registerAdapter(new CAParticle());
        registerAdapter(new CAMenuBuilder());
        registerAdapter(new CAMoveLength());
        registerAdapter(new CADynamicEnum());
        registerAdapter(new CAInterval());
        registerAdapter(new CALayoutShape());
        registerAdapter(new CAXBiome());
        registerAdapter(new CAXEnchantment());
        registerAdapter(new CAXEntityType());
        registerAdapter(new CAXMaterial());
        registerAdapter(new CAXPotion());
        registerAdapter(new CAXSound());
        registerAdapter(new CAOptional());
        registerAdapter(new CATimeLength());
        registerAdapter(new CACraftingRecipe());
    }
}
