package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.config.exc.InvalidConfigException;
import com.hypixel.hytale.server.core.ui.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Writes one menu as a single inline UI document plus the commands that must follow it.
 * <p>
 * Inline documents can't import other {@code .ui} files, so references to values in them,
 * like {@code @DefaultTextButtonStyle}, are sent as {@code Set} commands after the document.
 * </p>
 */
final class MenuMarkup {
    final StringBuilder out = new StringBuilder();
    /** Pairs of selector and value, for {@code UICommandBuilder#set}. */
    final List<Object[]> sets = new ArrayList<>();
    private int depth;

    void open(String type, String id) {
        indent().append(type).append(" #").append(id).append(" {\n");
        depth++;
    }

    void close() {
        depth--;
        indent().append("}\n");
    }

    /**
     * Writes a raw property line. Only for values KCommon itself controls.
     *
     * @param name  the property name.
     * @param value the literal markup.
     */
    void raw(String name, String value) {
        indent().append(name).append(": ").append(value).append(";\n");
    }

    void set(String selector, Object value) {
        sets.add(new Object[]{selector, value});
    }

    /**
     * Writes a property of the element, inline when it can be, otherwise as a {@code Set} command.
     *
     * @param id       the element's id.
     * @param property the property.
     * @param value    the value, with placeholders already applied.
     * @param path     the config path, for errors.
     */
    void property(String id, MenuSchema.Property property, Object value, String path) {
        String selector = "#" + id + "." + property.name;
        String[] ref = MenuSchema.ref(value);
        if (ref != null) {
            checkRef(property, path);
            set(selector, Value.ref(ref[0], ref[1]));
            return;
        }
        if (property.kind == MenuSchema.Kind.REF) {
            for (Object[] refSet : refMap(property, value, path)) {
                set("#" + id + "." + refSet[0], refSet[1]);
            }
            return;
        }
        raw(property.name, literal(property, value, path, false));
    }

    /**
     * Checks a value without writing it. Strings holding placeholders are checked when the menu is built instead.
     *
     * @param property the property.
     * @param value    the value.
     * @param path     the config path, for errors.
     */
    static void validate(MenuSchema.Property property, Object value, String path) {
        if (MenuSchema.ref(value) != null) {
            checkRef(property, path);
        } else if (property.kind == MenuSchema.Kind.REF) {
            refMap(property, value, path);
        } else {
            literal(property, value, path, true);
        }
    }

    /**
     * Quotes a string for markup.
     *
     * @param text the text.
     * @return the quoted text.
     */
    static String quote(String text) {
        StringBuilder out = new StringBuilder(text.length() + 2).append('"');
        for (char c : text.toCharArray()) {
            if (c == '"' || c == '\\') {
                out.append('\\').append(c);
            } else if (Character.isISOControl(c)) {
                // A raw line break or tab inside a markup string doesn't parse.
                out.append(' ');
            } else {
                out.append(c);
            }
        }
        return out.append('"').toString();
    }

    /**
     * Reads a whole number.
     *
     * @param value the value.
     * @param path  the config path, for errors.
     * @return the number as markup.
     */
    static String integer(Object value, String path) {
        return number(String.valueOf(value).trim(), path, true);
    }

    private static void checkRef(MenuSchema.Property property, String path) {
        switch (property.kind) {
            case TUPLE:
            case PATCH:
            case REF:
                return;
            default:
                throw new InvalidConfigException(path + " can't be a reference to a .ui value");
        }
    }

    /**
     * Reads a reference with overrides, such as {@code {ref: '@SecondaryButtonStyle', sounds: '@ButtonSounds'}}.
     */
    private static List<Object[]> refMap(MenuSchema.Property property, Object value, String path) {
        if (!(value instanceof Map))
            throw new InvalidConfigException(path + " must be a reference to a .ui value, such as '@DefaultTextButtonStyle'");

        List<Object[]> sets = new ArrayList<>();
        Object base = null;
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            String key = MenuSchema.pascal(String.valueOf(entry.getKey()));
            String[] ref = MenuSchema.ref(entry.getValue());
            if (ref == null)
                throw new InvalidConfigException(path + "." + entry.getKey() + " must be a reference to a .ui value, such as '@ButtonSounds'");
            if (key.equals("Ref")) {
                base = Value.ref(ref[0], ref[1]);
            } else if (key.equals("Sounds")) {
                sets.add(new Object[]{property.name + ".Sounds", Value.ref(ref[0], ref[1])});
            } else {
                throw new InvalidConfigException(path + "." + entry.getKey() + " isn't supported; only 'ref' and 'sounds' are");
            }
        }
        if (base == null)
            throw new InvalidConfigException(path + " needs a 'ref'");
        // The base value replaces the whole property, so it goes before any overrides.
        sets.add(0, new Object[]{property.name, base});
        return sets;
    }

    private static String literal(MenuSchema.Property property, Object value, String path, boolean lenient) {
        if (value instanceof Map) {
            if (property.kind != MenuSchema.Kind.TUPLE && property.kind != MenuSchema.Kind.PATCH)
                throw new InvalidConfigException(path + " must be a single value, not a section");
            return tuple(property, (Map<?, ?>) value, path, lenient);
        }
        if (value == null || value instanceof List)
            throw new InvalidConfigException(path + " must be a single value");
        if (lenient && value instanceof String && ((String) value).contains("{"))
            return "";

        String text = String.valueOf(value).trim();
        switch (property.kind) {
            case BOOL:
                if (!text.equals("true") && !text.equals("false"))
                    throw new InvalidConfigException(path + " must be true or false, not '" + text + "'");
                return text;
            case INTEGER:
                return number(text, path, true);
            case DECIMAL:
                return number(text, path, false);
            case STRING:
                return quote(String.valueOf(value));
            case COLOR:
                if (!MenuSchema.isColor(text))
                    throw new InvalidConfigException(path + " must be a color such as '#ffffff' or '#000000(0.5)', not '" + text + "'");
                return text;
            case ENUM:
                if (!property.values.contains(text))
                    throw new InvalidConfigException(path + " must be one of " + property.values + ", not '" + text + "'");
                return text;
            case PATCH:
                // A color fills the area; a texture path is relative to UI/Custom.
                if (MenuSchema.isColor(text))
                    return "(Color: " + text + ")";
                if (!text.endsWith(".png"))
                    throw new InvalidConfigException(path + " must be a color such as '#ffffff' or a .png texture path, not '" + text + "'");
                return "(TexturePath: " + quote(text) + ")";
            default:
                throw new InvalidConfigException(path + " must be a section of values, such as '{width: 100}'");
        }
    }

    private static String tuple(MenuSchema.Property property, Map<?, ?> values, String path, boolean lenient) {
        StringBuilder out = new StringBuilder("(");
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            String key = MenuSchema.pascal(String.valueOf(entry.getKey()));
            MenuSchema.Property sub = property.keys.get(key);
            String subPath = path + "." + entry.getKey();
            if (sub == null)
                throw new InvalidConfigException(subPath + " isn't a known key of " + property.name + "; known keys are " + property.keys.keySet());
            if (MenuSchema.ref(entry.getValue()) != null)
                throw new InvalidConfigException(subPath + " can't be a reference here; reference the whole " + property.name + " instead");
            if (out.length() > 1)
                out.append(", ");
            out.append(key).append(": ").append(literal(sub, entry.getValue(), subPath, lenient));
        }
        return out.append(')').toString();
    }

    private static String number(String text, String path, boolean integer) {
        BigDecimal number;
        try {
            number = new BigDecimal(text);
        } catch (NumberFormatException exc) {
            throw new InvalidConfigException(path + " must be a number, not '" + text + "'");
        }
        if (!integer)
            return number.stripTrailingZeros().toPlainString();
        try {
            return number.toBigIntegerExact().toString();
        } catch (ArithmeticException exc) {
            throw new InvalidConfigException(path + " must be a whole number, not '" + text + "'");
        }
    }

    private StringBuilder indent() {
        for (int i = 0; i < depth; i++) {
            out.append("  ");
        }
        return out;
    }
}
