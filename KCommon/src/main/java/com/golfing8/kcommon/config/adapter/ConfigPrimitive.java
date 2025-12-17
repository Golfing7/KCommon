package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigEntry;
import lombok.Getter;
import lombok.var;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents something that can be natively interpreted by YAML.
 * Examples include:
 * <ol>
 *     <li>String, int, double, etc.</li>
 *     <li>Lists, Maps (Basically config sections)</li>
 * </ol>
 *
 * <p>
 * Note that the {@link ConfigurationSection} class and its subtypes are NOT considered primitives.
 * The way they're handled is by converting their key/values into a non-flattened map and using that as its primitive stand-in.
 * </p>
 */
public final class ConfigPrimitive {
    private static final ConfigPrimitive NULL = new ConfigPrimitive(null);

    /**
     * The primitive object
     */
    @Getter
    @Nullable
    private final Object primitive;
    /**
     * The source of the data.
     */
    @Getter
    @Nullable
    private final ConfigurationSection source;

    private ConfigPrimitive(@Nullable Object value, @Nullable ConfigurationSection source) {
        this.source = source;
        this.primitive = value;
    }

    private ConfigPrimitive(@Nullable Object value) {
        this(value, null);
    }


    /**
     * Unwraps the given value. This will unwrap all config primitive types.
     *
     * @return the unwrapped value.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> T unwrap() {
        if (primitive instanceof Map) {
            Map items = new LinkedHashMap();
            Map m = (Map) primitive;
            m.forEach((k, v) -> {
                if (v instanceof ConfigPrimitive) {
                    items.put(k, ((ConfigPrimitive) v).unwrap());
                } else {
                    items.put(k, v);
                }
            });
            return (T) items;
        } else if (primitive instanceof List) {
            List toReturn = new ArrayList();
            List l = (List) primitive;
            for (Object thing : l) {
                if (thing instanceof ConfigPrimitive) {
                    toReturn.add(((ConfigPrimitive) thing).unwrap());
                } else {
                    toReturn.add(thing);
                }
            }
            return (T) toReturn;
        } else if (primitive instanceof ConfigPrimitive) {
            return ((ConfigPrimitive) primitive).unwrap();
        }
        return (T) primitive;
    }

    /**
     * Gets a value contained within this primitive, retaining the config section source.
     *
     * @param key the key
     * @return the primitive.
     */
    @SuppressWarnings({"rawtypes"})
    public ConfigPrimitive getSubValue(String key) {
        if (this.source == null) {
            return new ConfigPrimitive(((Map) this.getPrimitive()).get(key));
        }

        if (!this.source.isConfigurationSection(key)) {
            return new ConfigPrimitive(this.source.get(key), this.source);
        }

        return ConfigPrimitive.ofSection(this.source.getConfigurationSection(key));
    }

    /**
     * Formats the key onto the path that the {@link #source} source is under.
     *
     * @param key the key.
     * @return the formatted path.
     */
    public String formatPath(String key) {
        return source == null || !source.getCurrentPath().isEmpty() ? key : source.getCurrentPath() + "." + key;
    }

    /**
     * Constructs a config primitive from the given non-null value.
     * <p>
     * If the given value is not a primitively mappable type, it is rejected.
     * </p>
     *
     * @param value the value
     * @return the primitive
     */
    public static ConfigPrimitive of(@NotNull Object value) {
        // We should still handle sections.
        if (value instanceof ConfigurationSection) {
            return ofSection((ConfigurationSection) value);
        }

        if (value instanceof Number ||
                value instanceof String ||
                value instanceof Map ||
                value instanceof List ||
                value instanceof Boolean) {
            return new ConfigPrimitive(value);
        } else {
            throw new IllegalArgumentException(String.format("Value was not a primitive! Was %s", value.getClass()));
        }
    }

    /**
     * Constructs a config primitive from the given nullable value.
     * <p>
     * If the given value is not a primitively mappable type, it is rejected.
     * </p>
     *
     * @param value the value
     * @return the primitive
     */
    public static ConfigPrimitive ofNullable(@Nullable Object value) {
        if (value == null)
            return NULL;

        return of(value);
    }

    /**
     * Gets the {@link #NULL} config primitive instance
     *
     * @return the null config primitive
     */
    public static ConfigPrimitive ofNull() {
        return NULL;
    }

    /**
     * An internally available factory method for constructing a primitive with a trusted value
     *
     * @param value the value
     * @return the primitive
     */
    static ConfigPrimitive ofTrusted(Object value) {
        return new ConfigPrimitive(value);
    }

    /**
     * An internally available factory method for constructing a primitive with a trusted value
     *
     * @param value the value
     * @param source the config source
     * @return the primitive
     */
    static ConfigPrimitive ofTrusted(Object value, ConfigurationSection source) {
        return new ConfigPrimitive(value, source);
    }

    /**
     * Constructs a config primitive from the given entry
     *
     * @param entry the entry
     * @return the primitive
     */
    public static ConfigPrimitive ofValue(ConfigEntry entry) {
        Object value = entry.get();
        if (value instanceof ConfigurationSection) {
            return ofSection((ConfigurationSection) value);
        }
        return new ConfigPrimitive(entry.get(), entry.getSection());
    }

    /**
     * Constructs a config primitive from the given string
     *
     * @param string the string
     * @return the primitive
     */
    public static ConfigPrimitive ofString(String string) {
        return new ConfigPrimitive(string);
    }

    /**
     * Constructs a config primitive from the given int
     *
     * @param value the integer
     * @return the primitive
     */
    public static ConfigPrimitive ofInt(int value) {
        return new ConfigPrimitive(value);
    }

    /**
     * Constructs a config primitive from the given double
     *
     * @param value the double
     * @return the primitive
     */
    public static ConfigPrimitive ofDouble(double value) {
        return new ConfigPrimitive(value);
    }

    /**
     * Constructs a config primitive from the given list
     * <p>
     * This does not check the values contained within the list to check if they're also primitive
     * </p>
     *
     * @param list the list
     * @return the primitive
     */
    public static ConfigPrimitive ofList(List<?> list) {
        return new ConfigPrimitive(list);
    }

    /**
     * Constructs a config primitive from the given map
     * <p>
     * This does not check the values contained within the map to check if they're also primitive
     * </p>
     *
     * @param map the map
     * @return the primitive
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static ConfigPrimitive ofMap(Map<String, ?> map) {
        Map safeValues = new LinkedHashMap();
        for (var entry : map.entrySet()) {
            String realKey = stringToSafeKeyString(entry.getKey());
            safeValues.put(realKey, entry.getValue());
        }
        return new ConfigPrimitive(safeValues);
    }

    /**
     * Constructs a config primitive from the given config section
     *
     * @param section the config section
     * @return the primitive
     */
    public static ConfigPrimitive ofSection(ConfigurationSection section) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                values.put(key, ConfigPrimitive.ofSection(section.getConfigurationSection(key)));
            } else {
                values.put(key, section.get(key));
            }
        }

        return new ConfigPrimitive(values, section);
    }

    /**
     * Converts the given string to a safe key that can be used in the config
     *
     * @param original the original string
     * @return the safe key string
     */
    public static String stringToSafeKeyString(String original) {
        return original.replace(".", ",");
    }

    /**
     * Converts the given safe key string back to a normal string
     *
     * @param original the safe key string
     * @return the normal string
     */
    public static String safeKeyStringToString(String original) {
        return original.replace(",", ".");
    }

    /**
     * Tries to coerce java-like objects to strings such as Integer or Double.
     *
     * @param value the value.
     * @return the string representation.
     */
    public static String coerceBoxedToString(Object value) {
        return value.toString();
    }

    /**
     * Checks if the given type is a primitive type.
     *
     * @param type the type.
     * @return true if its a primitive type.
     */
    public static boolean isYamlPrimitive(Class<?> type) {
        if (type == Integer.class) {
            return true;
        } else if (type == Short.class) {
            return true;
        } else if (type == Byte.class) {
            return true;
        } else if (type == Long.class) {
            return true;
        } else if (type == Double.class) {
            return true;
        } else if (type == Float.class) {
            return true;
        } else if (type == Boolean.class) {
            return true;
        } else return type == String.class;
    }

    /**
     * Tries to coerce a string back into its java box object.
     *
     * @param val       the string.
     * @param boxedType the boxed type of object
     * @return the boxed object.
     */
    public static Object coerceObjectToBoxed(Object val, Class<?> boxedType) {
        if (boxedType == Integer.class) {
            return Integer.parseInt(val.toString());
        } else if (boxedType == Short.class) {
            return Short.parseShort(val.toString());
        } else if (boxedType == Byte.class) {
            return Byte.parseByte(val.toString());
        } else if (boxedType == Long.class) {
            return Long.parseLong(val.toString());
        } else if (boxedType == Double.class) {
            return Double.parseDouble(val.toString().replace(",", "."));
        } else if (boxedType == Float.class) {
            return Float.parseFloat(val.toString().replace(",", "."));
        } else if (boxedType == Boolean.class) {
            return Boolean.parseBoolean(val.toString());
        } else {
            return val;
        }
    }
}
