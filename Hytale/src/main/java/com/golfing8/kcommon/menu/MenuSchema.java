package com.golfing8.kcommon.menu;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The elements, properties and values a menu may put into Hytale UI markup.
 * <p>
 * The Hytale client disconnects a player when a page holds an unknown element, property or value,
 * so this is deliberately a whitelist. Every name here comes from the client's markup definitions
 * and the game's own {@code .ui} files.
 * </p>
 */
final class MenuSchema {
    /**
     * Kinds of values a property holds.
     */
    enum Kind {
        BOOL,
        INTEGER,
        DECIMAL,
        STRING,
        COLOR,
        ENUM,
        /** A tuple such as {@code Anchor: (Width: 10)}, with known keys. */
        TUPLE,
        /** A patch style, written as a tuple, a texture path or a color. */
        PATCH,
        /** Only a reference into a {@code .ui} document, such as {@code @DefaultTextButtonStyle}. */
        REF
    }

    /**
     * A property, its value kind, and for enums and tuples the values or keys it accepts.
     */
    static final class Property {
        final String name;
        final Kind kind;
        final Set<String> values;
        final Map<String, Property> keys;
        /** For tuples, patches and references, the type of {@code .ui} value it holds (such as {@code TextButtonStyle}), for tooling. */
        final @Nullable String uiType;

        Property(String name, Kind kind, Set<String> values, Map<String, Property> keys, @Nullable String uiType) {
            this.name = name;
            this.kind = kind;
            this.values = values;
            this.keys = keys;
            this.uiType = uiType;
        }
    }

    private static final Pattern COLOR = Pattern.compile("#[0-9a-fA-F]{6}(\\(\\s*(0(\\.\\d+)?|1(\\.0+)?)\\s*\\))?");
    private static final Pattern REF = Pattern.compile("(?:([A-Za-z0-9_/]+\\.ui))?@([A-Za-z][A-Za-z0-9]*)");
    private static final Set<String> ALIGNMENTS = set("Start", "Center", "End");
    private static final Set<String> LAYOUT_MODES = set("Full", "Left", "Center", "Right", "Top", "Middle", "Bottom",
            "LeftScrolling", "RightScrolling", "TopScrolling", "BottomScrolling", "CenterMiddle", "MiddleCenter",
            "LeftCenterWrap", "LeftWrap");

    static final Property ANCHOR = tuple("Anchor", "Anchor", integer("Width"), integer("Height"), integer("Top"), integer("Bottom"),
            integer("Left"), integer("Right"), integer("Full"), integer("Horizontal"), integer("Vertical"),
            integer("MinWidth"), integer("MaxWidth"));
    static final Property PADDING = tuple("Padding", "Padding", integer("Top"), integer("Bottom"), integer("Left"), integer("Right"),
            integer("Full"), integer("Horizontal"), integer("Vertical"));
    static final Property LABEL_STYLE = tuple("Style", "LabelStyle", string("FontName"), integer("FontSize"),
            enumOf("HorizontalAlignment", ALIGNMENTS), enumOf("VerticalAlignment", ALIGNMENTS), decimal("LetterSpacing"),
            integer("MinShrinkTextToFitFontSize"), color("OutlineColor"), bool("RenderBold"), bool("RenderItalics"),
            bool("RenderUnderlined"), bool("RenderUppercase"), bool("ShrinkTextToFit"), color("TextColor"), bool("Wrap"),
            integer("WrapMaxLines"));
    static final Map<String, Property> PATCH_KEYS = keys(string("TexturePath"), integer("Border"),
            integer("HorizontalBorder"), integer("VerticalBorder"), color("Color"));

    /** Types that may hold children. */
    static final Set<String> CONTAINERS = set("Group", "Button", "AssetImage");
    /** Types whose {@code text} becomes the element's text. */
    static final Set<String> TEXT_TYPES = set("Label", "TextButton", "Title", "StyledText");
    /** Types that send their value back when an event fires on them. */
    static final Set<String> VALUE_TYPES = set("TextField");

    private static final Map<String, Map<String, Property>> ELEMENTS = new LinkedHashMap<>();

    static {
        Property[] common = {ANCHOR, PADDING, patch("Background"), integer("FlexWeight"), bool("Visible"),
                bool("HitTestVisible"), string("TooltipText"), bool("Overscroll"), string("MaskTexturePath"),
                bool("KeepScrollPosition"), bool("AutoScrollDown")};
        element("Group", common, enumOf("LayoutMode", LAYOUT_MODES), ref("ScrollbarStyle", "ScrollbarStyle"));
        element("Label", common, LABEL_STYLE);
        element("Title", common, LABEL_STYLE);
        // StyledText is a Group of one Label per character; FontSize and Bold style those labels.
        element("StyledText", common, enumOf("LayoutMode", LAYOUT_MODES), integer("FontSize"), bool("Bold"));
        element("TextButton", common, ref("Style", "TextButtonStyle"), bool("Disabled"));
        element("Button", common, ref("Style", "ButtonStyle"), bool("Disabled"), enumOf("LayoutMode", LAYOUT_MODES));
        element("ItemIcon", common, string("ItemId"), bool("ShowItemTooltip"));
        element("ItemSlot", common, string("ItemId"), integer("Quantity"), bool("ShowQuantity"),
                bool("ShowQualityBackground"), bool("ShowDurabilityBar"));
        element("AssetImage", common, string("AssetPath"), string("FallbackTexturePath"));
        element("ProgressBar", common, decimal("Value"), patch("Bar"), string("BarTexturePath"),
                enumOf("Alignment", set("Vertical", "Horizontal")), enumOf("Direction", set("Start", "End")));
        element("TextField", common, string("PlaceholderText"), ref("Style", "InputFieldStyle"), ref("PlaceholderStyle", "InputFieldStyle"),
                integer("MaxLength"), bool("AutoFocus"), bool("IsReadOnly"));
    }

    private MenuSchema() {
    }

    /**
     * Gets the properties an element type accepts.
     *
     * @param type the element type.
     * @return the properties by name, or null if the type is unknown.
     */
    static @Nullable Map<String, Property> properties(String type) {
        return ELEMENTS.get(type);
    }

    /**
     * Turns a config key such as {@code layout-mode} into a markup name such as {@code LayoutMode}.
     *
     * @param key the key.
     * @return the markup name.
     */
    static String pascal(String key) {
        StringBuilder out = new StringBuilder(key.length());
        boolean upper = true;
        for (char c : key.toCharArray()) {
            if (c == '-' || c == '_' || c == ' ') {
                upper = true;
            } else {
                out.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return out.toString();
    }

    /**
     * Parses a reference such as {@code @DefaultTextButtonStyle} or {@code Pages/Foo.ui@Bar}.
     *
     * @param value the value.
     * @return the document and value name, or null if this isn't a reference.
     */
    static String @Nullable [] ref(Object value) {
        if (!(value instanceof String))
            return null;
        Matcher matcher = REF.matcher(((String) value).trim());
        if (!matcher.matches())
            return null;
        return new String[]{matcher.group(1) == null ? "Common.ui" : matcher.group(1), matcher.group(2)};
    }

    /**
     * Checks a color literal such as {@code #ffffff} or {@code #000000(0.45)}.
     *
     * @param value the value.
     * @return true if it is a color.
     */
    static boolean isColor(String value) {
        return COLOR.matcher(value).matches();
    }

    /**
     * Writes this whitelist as JSON, for tools such as the KCommon IntelliJ plugin. It is kept in
     * {@code kcommon/menu-schema.json}, and a test fails when that file is out of date.
     *
     * @return the JSON.
     */
    static String toJson() {
        StringBuilder out = new StringBuilder("{\n");
        out.append("  \"containers\": ").append(strings(CONTAINERS)).append(",\n");
        out.append("  \"textTypes\": ").append(strings(TEXT_TYPES)).append(",\n");
        out.append("  \"valueTypes\": ").append(strings(VALUE_TYPES)).append(",\n");
        out.append("  \"elements\": {");
        String separator = "\n";
        for (Map.Entry<String, Map<String, Property>> element : ELEMENTS.entrySet()) {
            out.append(separator).append("    \"").append(element.getKey()).append("\": ").append(properties(element.getValue()));
            separator = ",\n";
        }
        return out.append("\n  }\n}\n").toString();
    }

    private static String properties(Map<String, Property> properties) {
        StringBuilder out = new StringBuilder("{");
        String separator = "";
        for (Property property : properties.values()) {
            out.append(separator).append('"').append(property.name).append("\": {\"kind\": \"").append(property.kind).append('"');
            if (property.uiType != null)
                out.append(", \"uiType\": \"").append(property.uiType).append('"');
            if (!property.values.isEmpty())
                out.append(", \"values\": ").append(strings(property.values));
            if (!property.keys.isEmpty())
                out.append(", \"keys\": ").append(properties(property.keys));
            out.append('}');
            separator = ", ";
        }
        return out.append('}').toString();
    }

    private static String strings(Set<String> values) {
        StringBuilder out = new StringBuilder("[");
        String separator = "";
        for (String value : new java.util.TreeSet<>(values)) {
            out.append(separator).append('"').append(value).append('"');
            separator = ", ";
        }
        return out.append(']').toString();
    }

    private static void element(String type, Property[] common, Property... own) {
        Map<String, Property> properties = new LinkedHashMap<>(keys(common));
        properties.putAll(keys(own));
        ELEMENTS.put(type, Collections.unmodifiableMap(properties));
    }

    private static Map<String, Property> keys(Property... properties) {
        Map<String, Property> map = new LinkedHashMap<>();
        for (Property property : properties) {
            map.put(property.name, property);
        }
        return Collections.unmodifiableMap(map);
    }

    private static Set<String> set(String... values) {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(values)));
    }

    private static Property simple(String name, Kind kind) {
        return new Property(name, kind, Collections.emptySet(), Collections.emptyMap(), null);
    }

    private static Property bool(String name) {
        return simple(name, Kind.BOOL);
    }

    private static Property integer(String name) {
        return simple(name, Kind.INTEGER);
    }

    private static Property decimal(String name) {
        return simple(name, Kind.DECIMAL);
    }

    private static Property string(String name) {
        return simple(name, Kind.STRING);
    }

    private static Property color(String name) {
        return simple(name, Kind.COLOR);
    }

    private static Property ref(String name, String uiType) {
        return new Property(name, Kind.REF, Collections.emptySet(), Collections.emptyMap(), uiType);
    }

    private static Property enumOf(String name, Set<String> values) {
        return new Property(name, Kind.ENUM, values, Collections.emptyMap(), null);
    }

    private static Property tuple(String name, String uiType, Property... keys) {
        return new Property(name, Kind.TUPLE, Collections.emptySet(), keys(keys), uiType);
    }

    private static Property patch(String name) {
        return new Property(name, Kind.PATCH, Collections.emptySet(), PATCH_KEYS, "PatchStyle");
    }
}
