package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.filter.StringFilter;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * An adapter for all {@link CAStringFilter} types.
 */
public class CAStringFilter implements ConfigAdapter<StringFilter> {
    @Override
    public Class<StringFilter> getAdaptType() {
        return StringFilter.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringFilter toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        // Handle basic filters
        if (entry.getPrimitive() instanceof String) {
            return new StringFilter(entry.getPrimitive().toString(), false, false, false);
        }

        Map<String, Object> items = (Map<String, Object>) entry.getPrimitive();
        boolean ignoreCase = (boolean) items.getOrDefault("ignore-case", false);
        boolean contains = (boolean) items.getOrDefault("contains", false);
        boolean regex = (boolean) items.getOrDefault("regex", false);
        String pattern = (String) items.get("pattern");
        return new StringFilter(pattern, ignoreCase, contains, regex);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull StringFilter object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        Map<String, Object> items = new HashMap<>();
        if (object.isSimple()) {
            return ConfigPrimitive.ofString(object.getPattern());
        }

        items.put("ignore-case", object.isIgnoreCase());
        items.put("contains", object.isContains());
        items.put("regex", object.isRegex());
        items.put("pattern", object.getPattern());
        return ConfigPrimitive.ofMap(items);
    }
}
