package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.config.exc.InvalidConfigException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One element of a menu as configured, before placeholders and bindings are applied.
 */
final class MenuElement {
    final String key;
    final String type;
    final boolean special;
    final @Nullable String text;
    /** Properties by markup name, in config order. */
    final Map<String, Object> properties;
    final List<MenuElement> children;
    final String path;

    private MenuElement(String key, String type, boolean special, @Nullable String text,
                        Map<String, Object> properties, List<MenuElement> children, String path) {
        this.key = key;
        this.type = type;
        this.special = special;
        this.text = text;
        this.properties = properties;
        this.children = children;
        this.path = path;
    }

    /**
     * Gets the property definition this element type has under the name.
     *
     * @param name the markup name.
     * @return the property, or null if this type has none by that name.
     */
    @Nullable MenuSchema.Property property(String name) {
        Map<String, MenuSchema.Property> properties = MenuSchema.properties(type);
        return properties == null ? null : properties.get(name);
    }

    /**
     * Finds an element with the key in this tree.
     *
     * @param key the key.
     * @return the element, or null.
     */
    @Nullable MenuElement find(String key) {
        if (this.key.equals(key))
            return this;
        for (MenuElement child : children) {
            MenuElement found = child.find(key);
            if (found != null)
                return found;
        }
        return null;
    }

    /**
     * Parses an element.
     *
     * @param key         the element's key.
     * @param raw         the config value.
     * @param defaultType the type when none is configured.
     * @param path        the config path, for errors.
     * @param seen        the keys already used in this tree, so lookups by key stay unambiguous.
     * @return the element.
     */
    @SuppressWarnings("unchecked")
    static MenuElement parse(String key, Object raw, String defaultType, String path, Set<String> seen) {
        if (!(raw instanceof Map))
            throw new InvalidConfigException(path + " must be a section describing an element");
        if (!seen.add(key))
            throw new InvalidConfigException(path + ": the key '" + key + "' is used twice in the same menu or template");

        Map<String, Object> section = (Map<String, Object>) raw;
        String type = section.containsKey("type") ? String.valueOf(section.get("type")) : defaultType;
        Map<String, MenuSchema.Property> known = MenuSchema.properties(type);
        if (known == null)
            throw new InvalidConfigException(path + ".type '" + type + "' isn't a supported element type");

        boolean special = false;
        String text = null;
        Map<String, Object> properties = new LinkedHashMap<>();
        List<MenuElement> children = new ArrayList<>();
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            String option = entry.getKey();
            Object value = entry.getValue();
            String optionPath = path + "." + option;
            switch (option) {
                case "type":
                    break;
                case "special":
                    special = Boolean.TRUE.equals(value) || "true".equals(String.valueOf(value));
                    break;
                case "text":
                    if (!MenuSchema.TEXT_TYPES.contains(type))
                        throw new InvalidConfigException(optionPath + ": a " + type + " can't have text");
                    text = value instanceof List ? String.join("\n", (List<String>) value) : String.valueOf(value);
                    break;
                case "children":
                    if (!MenuSchema.CONTAINERS.contains(type))
                        throw new InvalidConfigException(optionPath + ": a " + type + " can't have children");
                    if (!(value instanceof Map))
                        throw new InvalidConfigException(optionPath + " must be a section of elements");
                    for (Map.Entry<String, Object> child : ((Map<String, Object>) value).entrySet()) {
                        children.add(parse(child.getKey(), child.getValue(), "Group", optionPath + "." + child.getKey(), seen));
                    }
                    break;
                default:
                    String name = MenuSchema.pascal(option);
                    MenuSchema.Property property = known.get(name);
                    if (property == null)
                        throw new InvalidConfigException(optionPath + " isn't a property of " + type + "; known properties are " + known.keySet());
                    MenuMarkup.validate(property, value, optionPath);
                    properties.put(name, value);
                    break;
            }
        }
        return new MenuElement(key, type, special, text, Collections.unmodifiableMap(properties),
                Collections.unmodifiableList(children), path);
    }
}
