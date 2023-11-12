package com.golfing8.kcommon.config;

import com.cryptomorin.xseries.XPotion;
import com.golfing8.kcommon.config.adapter.*;
import com.golfing8.kcommon.menu.MenuUtils;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.filter.ItemFilter;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents common things for configurations across the plugin.
 */
public class ConfigTypeRegistry {
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
    @SuppressWarnings({"unchecked"})
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setInConfig(ConfigurationSection section, String path, Object value) {
        ConfigAdapter adapter = findAdapter(value.getClass());
        if (adapter == null) {
            // Use default set.
            section.set(path, value);
            return;
        }

        section.set(path, adapter.toPrimitive(value).getPrimitive());
    }

    // Register some common types.
    static {
        registerAdapter(new CAEnum());
        registerAdapter(new CARegion());
        registerAdapter(new CAList());
        registerAdapter(new CAMap());
        registerAdapter(new CAItemStackBuilder());
        registerAdapter(new CAXMaterial());
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

        registerAdapter(MenuCoordinate.class, (section) -> {
            if(section.contains("slot")) {
                //Check if they're using cartesian style coordinates.
                if(section.contains("slot.x")) {
                    int yCoordinate = section.getInt("slot.y");
                    int xCoordinate = section.getInt("slot.x");

                    //Check the validity of the coordinates.
                    if(xCoordinate < 1 || xCoordinate > 6)
                        throw new ImproperlyConfiguredValueException(section.getConfigurationSection("slot"), "x", "A value 1-6");
                    if(yCoordinate < 1 || yCoordinate > 9)
                        throw new ImproperlyConfiguredValueException(section.getConfigurationSection("slot"), "y", "A value 1-9");

                    return new MenuCoordinate(xCoordinate, yCoordinate, section.getInt("page"));
                }else {
                    MenuCoordinate slot = MenuUtils.getCartCoordsFromSlot(section.getInt("slot"));
                    slot.setPage(section.getInt("page"));
                    return slot;
                }
            }else {
                throw new ImproperlyConfiguredValueException(section, "slot", "a 'slot' key");
            }
        }, (section, coord) -> {
            section.set("slot.x", coord.getX());
            section.set("slot.y", coord.getY());
        });

        registerAdapter(PotionEffect.class, (section) -> {
            Optional<XPotion> xPotionOptional = XPotion.matchXPotion(section.getString("type"));
            if(!xPotionOptional.isPresent())
                throw new ImproperlyConfiguredValueException(section, "type");

            XPotion xPotion = xPotionOptional.get();
            int duration = section.getInt("duration");
            int level = section.getInt("level");

            //Build the effect.
            return xPotion.buildPotionEffect(duration, level);
        }, (section, pot) -> {
            section.set("type", XPotion.matchXPotion(pot.getType()).name());
            section.set("duration", pot.getDuration());
            section.set("level", pot.getAmplifier());
        });
    }
}
