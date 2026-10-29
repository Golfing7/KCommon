package com.golfing8.kcommon.config.yaml;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.KSection;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A section of a {@link YamlConfig}, backed by an ordered map.
 * <p>
 * Behaves like Bukkit's {@code MemorySection}: paths are separated by '.', numbers are converted between types
 * where possible, and maps set as values only become sections once the config is reloaded.
 * </p>
 */
public class YamlSection implements KSection {
    private static final char SEPARATOR = '.';

    protected final Map<String, Object> values = new LinkedHashMap<>();
    private final @Nullable YamlSection parent;
    private final String name;

    YamlSection(@Nullable YamlSection parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    /**
     * Gets the config this section belongs to.
     *
     * @return the config.
     */
    public YamlConfig getConfig() {
        YamlSection section = this;
        while (section.parent != null) {
            section = section.parent;
        }
        return (YamlConfig) section;
    }

    @Override
    public KSection getRootSection() {
        return getConfig();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCurrentPath() {
        if (parent == null)
            return "";

        String parentPath = parent.getCurrentPath();
        return parentPath.isEmpty() ? name : parentPath + SEPARATOR + name;
    }

    private String fullPath(String path) {
        String current = getCurrentPath();
        return current.isEmpty() ? path : current + SEPARATOR + path;
    }

    /**
     * Finds the section holding the last element of the path.
     *
     * @param path   the path.
     * @param create if missing sections should be created.
     * @return the section, or null if it doesn't exist and create is false.
     */
    private @Nullable YamlSection sectionFor(String path, boolean create) {
        YamlSection section = this;
        int start = 0;
        int index;
        while ((index = path.indexOf(SEPARATOR, start)) != -1) {
            String key = path.substring(start, index);
            Object value = section.values.get(key);
            if (!(value instanceof YamlSection)) {
                if (!create)
                    return null;

                YamlSection created = new YamlSection(section, key);
                section.values.put(key, created);
                value = created;
            }
            section = (YamlSection) value;
            start = index + 1;
        }
        return section;
    }

    private static String lastKey(String path) {
        return path.substring(path.lastIndexOf(SEPARATOR) + 1);
    }

    @Override
    public Object get(String path) {
        return get(path, (Object) null);
    }

    @Override
    public Object get(String path, Object def) {
        if (path.isEmpty())
            return this;

        YamlSection section = sectionFor(path, false);
        if (section == null)
            return def;

        Object value = section.values.get(lastKey(path));
        return value == null ? def : value;
    }

    @Override
    public void set(String path, Object value) {
        if (value instanceof ConfigPrimitive) {
            value = ((ConfigPrimitive) value).unwrap();
        } else if (value != null && !(value instanceof YamlSection) && ConfigPrimitive.asSection(value) == null && !isPlainYaml(value)) {
            // Let the adapters turn the object into something YAML can store.
            value = ConfigTypeRegistry.toPrimitive(value).unwrap();
        }

        YamlSection section = sectionFor(path, value != null);
        if (section == null)
            return;

        String key = lastKey(path);
        if (value == null) {
            section.values.remove(key);
        } else if (value instanceof YamlSection) {
            YamlSection copy = new YamlSection(section, key);
            copy.copyFrom((YamlSection) value);
            section.values.put(key, copy);
        } else {
            section.values.put(key, value);
        }
        getConfig().setModified(true);
    }

    private static boolean isPlainYaml(Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean
                || value instanceof Map || value instanceof List || value instanceof Character;
    }

    private void copyFrom(YamlSection other) {
        for (Map.Entry<String, Object> entry : other.values.entrySet()) {
            if (entry.getValue() instanceof YamlSection) {
                YamlSection child = new YamlSection(this, entry.getKey());
                child.copyFrom((YamlSection) entry.getValue());
                values.put(entry.getKey(), child);
            } else {
                values.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Fills this section from a map read by SnakeYAML, turning nested maps into sections.
     *
     * @param map the map.
     */
    void loadFrom(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            if (entry.getValue() instanceof Map) {
                YamlSection child = new YamlSection(this, key);
                child.loadFrom((Map<?, ?>) entry.getValue());
                values.put(key, child);
            } else {
                values.put(key, entry.getValue());
            }
        }
    }

    /**
     * Converts this section back into plain maps for SnakeYAML.
     *
     * @return the map.
     */
    Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            map.put(entry.getKey(), value instanceof YamlSection ? ((YamlSection) value).toMap() : value);
        }
        return map;
    }

    @Override
    public boolean contains(String path) {
        return get(path) != null;
    }

    @Override
    public Set<String> getKeys(boolean deep) {
        Set<String> keys = new LinkedHashSet<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            keys.add(entry.getKey());
            if (deep && entry.getValue() instanceof YamlSection) {
                for (String child : ((YamlSection) entry.getValue()).getKeys(true)) {
                    keys.add(entry.getKey() + SEPARATOR + child);
                }
            }
        }
        return keys;
    }

    @Override
    public boolean isConfigurationSection(String path) {
        return get(path) instanceof YamlSection;
    }

    @Override
    public @Nullable YamlSection getConfigurationSection(String path) {
        Object value = get(path);
        return value instanceof YamlSection ? (YamlSection) value : null;
    }

    @Override
    public YamlSection createSection(String path) {
        YamlSection section = sectionFor(path, true);
        YamlSection created = new YamlSection(section, lastKey(path));
        section.values.put(lastKey(path), created);
        getConfig().setModified(true);
        return created;
    }

    @Override
    public String getString(String path) {
        return getString(path, null);
    }

    @Override
    public String getString(String path, String def) {
        Object value = get(path, def);
        return value != null ? value.toString() : def;
    }

    @Override
    public int getInt(String path) {
        return getInt(path, 0);
    }

    @Override
    public int getInt(String path, int def) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).intValue() : parse(value, def, Integer::parseInt);
    }

    @Override
    public long getLong(String path) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).longValue() : parse(value, 0L, Long::parseLong);
    }

    @Override
    public double getDouble(String path) {
        Object value = get(path);
        return value instanceof Number ? ((Number) value).doubleValue() : parse(value, 0D, Double::parseDouble);
    }

    private static <T> T parse(@Nullable Object value, T def, java.util.function.Function<String, T> parser) {
        if (value == null)
            return def;

        try {
            return parser.apply(value.toString());
        } catch (NumberFormatException exc) {
            return def;
        }
    }

    @Override
    public boolean getBoolean(String path) {
        return getBoolean(path, false);
    }

    @Override
    public boolean getBoolean(String path, boolean def) {
        Object value = get(path);
        return value instanceof Boolean ? (Boolean) value : def;
    }

    @Override
    public List<?> getList(String path) {
        Object value = get(path);
        return value instanceof List ? (List<?>) value : null;
    }

    @Override
    public List<String> getStringList(String path) {
        List<?> list = getList(path);
        if (list == null)
            return new ArrayList<>();

        List<String> strings = new ArrayList<>();
        for (Object element : list) {
            if (element instanceof String || element instanceof Number || element instanceof Boolean || element instanceof Character) {
                strings.add(element.toString());
            }
        }
        return strings;
    }

    @Override
    public void set(String path, Object value, String... comments) {
        setComments(path, comments);
        set(path, value);
    }

    @Override
    public void setComments(String path, String... comments) {
        getConfig().setComments(fullPath(path), comments);
    }

    @Override
    public void ensureExists(String path) {
        getConfig().ensureExists(fullPath(path));
    }

    @Override
    public boolean tryLoadFromSource(String path) {
        return getConfig().tryLoadFromSource(fullPath(path));
    }

    @Override
    public boolean isModified() {
        return getConfig().isModified();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[path='" + getCurrentPath() + "', root='" + getConfig().getName() + "']";
    }
}
