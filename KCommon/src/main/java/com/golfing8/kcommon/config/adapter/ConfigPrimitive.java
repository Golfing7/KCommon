package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigEntry;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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

    /** The primitive object */
    @Getter @Nullable
    private final Object primitive;
    /** The source of the data. */
    @Getter @Nullable
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
            Map items = new HashMap();
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
            List l = (List) primitive;
            for (int i = 0; i < l.size(); i++) {
                Object thing = l.get(i);
                if (thing instanceof ConfigPrimitive) {
                    l.set(i, ((ConfigPrimitive) thing).unwrap());
                } else {
                    l.set(i, thing);
                }
            }
            return (T) l;
        } else if (primitive instanceof ConfigPrimitive) {
            return ((ConfigPrimitive) primitive).unwrap();
        }
        return (T) primitive;
    }

    public static ConfigPrimitive of(Object value) {
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

    public static ConfigPrimitive ofNull() {
        return NULL;
    }

    static ConfigPrimitive ofTrusted(Object value) {
        return new ConfigPrimitive(value);
    }

    public static ConfigPrimitive ofValue(ConfigEntry entry) {
        Object value = entry.get();
        if (value instanceof ConfigurationSection) {
            return ofSection((ConfigurationSection) value);
        }
        return new ConfigPrimitive(entry.get(), entry.getSection());
    }

    public static ConfigPrimitive ofString(String string) {
        return new ConfigPrimitive(string);
    }

    public static ConfigPrimitive ofInt(int value) {
        return new ConfigPrimitive(value);
    }

    public static ConfigPrimitive ofDouble(double value) {
        return new ConfigPrimitive(value);
    }

    public static ConfigPrimitive ofList(List<?> list) {
        return new ConfigPrimitive(list);
    }

    public static ConfigPrimitive ofMap(Map<String, ?> map) {
        return new ConfigPrimitive(map);
    }

    public static ConfigPrimitive ofSection(ConfigurationSection section) {
        Map<String, Object> values = new HashMap<>();
        for (String key : section.getKeys(false)) {
            if (section.isConfigurationSection(key)) {
                values.put(key, ConfigPrimitive.ofSection(section.getConfigurationSection(key)));
            } else {
                values.put(key, section.get(key));
            }
        }

        return new ConfigPrimitive(values, section);
    }
}
