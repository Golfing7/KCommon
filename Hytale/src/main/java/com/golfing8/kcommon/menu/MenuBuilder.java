package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.config.exc.InvalidConfigException;
import com.golfing8.kcommon.struct.placeholder.PlaceholderContainer;
import com.golfing8.kcommon.util.HytaleMessages;
import com.golfing8.kcommon.util.StyledTextExtractor;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A Hytale menu loaded from config, the Hytale counterpart of the Bukkit {@code MenuBuilder}.
 * <p>
 * Structure lives in YAML and behaviour in Java. The YAML describes a tree of elements; Java binds
 * actions and placeholders to elements by key, and adds copies of templates to lists with
 * {@link #addRow(String, String)}. {@link #build()} turns it into one inline UI document, so plugins
 * need no {@code .ui} asset files.
 * </p>
 * <pre>{@code
 * shop-menu:
 *   title: '&eShop'
 *   width: 600
 *   height: 500
 *   elements-per-page: 10
 *   content:
 *     layout-mode: Top
 *   elements:
 *     items:
 *       layout-mode: TopScrolling
 *       flex-weight: 1
 *     close:
 *       type: TextButton
 *       special: true
 *       style: '@CancelTextButtonStyle'
 *       anchor: {height: 44}
 *       text: Close
 *   templates:
 *     item:
 *       layout-mode: Left
 *       children:
 *         name: {type: Label, text: '{NAME}'}
 *         buy: {type: TextButton, style: '@DefaultTextButtonStyle', text: Buy}
 * }</pre>
 * <p>
 * Elements default to {@code Group}. Element and property names are Hytale's markup names in kebab case,
 * checked when the menu loads, because the client disconnects players on markup it doesn't know.
 * A value starting with {@code @} references a value of {@code Common.ui}, such as {@code @DefaultTextButtonStyle};
 * {@code Pages/Foo.ui@Bar} references another document. Elements marked {@code special} only appear when
 * Java binds or shows them. {@code Title} is a label styled like the game's titles, and {@code StyledText}
 * renders every character as its own label, for gradients.
 * </p>
 */
public class MenuBuilder extends MenuScope<MenuBuilder> {
    /** The key of the binding id in the data the client sends back. */
    static final String ACTION_KEY = "Action";
    /** The key of the bound element's value in the data the client sends back. */
    static final String VALUE_KEY = "@Value";
    private static final String CONTENT_KEY = "content";
    private static final AtomicLong GENERATIONS = new AtomicLong();
    private static final Map<String, Object> TITLE_STYLE = map("FontSize", 15, "VerticalAlignment", "Center",
            "RenderUppercase", true, "TextColor", "#b4c8c9", "FontName", "Secondary", "RenderBold", true,
            "LetterSpacing", 0, "HorizontalAlignment", "Center");

    private @Nullable String title;
    private final int width;
    private final int height;
    private final boolean overlay;
    private final boolean decorated;
    @Getter
    private final CustomPageLifetime lifetime;
    /** How many elements a {@link PagedMenuContainer} shows per page, 0 for all of them. */
    @Getter
    private final int elementsPerPage;
    private final MenuElement content;
    private final Map<String, MenuElement> templates = new LinkedHashMap<>();
    private final Map<String, List<MenuRow>> rows = new LinkedHashMap<>();

    /**
     * Loads a menu from its config section.
     *
     * @param section the section, as plain maps and lists.
     * @param path    the section's path, for errors.
     */
    @SuppressWarnings("unchecked")
    public MenuBuilder(Map<String, Object> section, String path) {
        String title = null;
        int width = 600;
        int height = 400;
        boolean overlay = false;
        boolean decorated = true;
        CustomPageLifetime lifetime = CustomPageLifetime.CanDismiss;
        int elementsPerPage = 0;
        Map<String, Object> content = new LinkedHashMap<>();
        Object elements = null;
        for (Map.Entry<String, Object> entry : section.entrySet()) {
            String optionPath = path + "." + entry.getKey();
            Object value = entry.getValue();
            switch (entry.getKey()) {
                case "title":
                    title = value instanceof List ? String.join("\n", (List<String>) value) : String.valueOf(value);
                    break;
                case "width":
                    width = integer(value, optionPath);
                    break;
                case "height":
                    height = integer(value, optionPath);
                    break;
                case "overlay":
                    overlay = Boolean.parseBoolean(String.valueOf(value));
                    break;
                case "frame":
                    String frame = String.valueOf(value).toLowerCase(Locale.ROOT);
                    if (!frame.equals("decorated") && !frame.equals("none"))
                        throw new InvalidConfigException(optionPath + " must be 'decorated' or 'none'");
                    decorated = frame.equals("decorated");
                    break;
                case "lifetime":
                    lifetime = lifetime(String.valueOf(value), optionPath);
                    break;
                case "elements-per-page":
                    elementsPerPage = integer(value, optionPath);
                    break;
                case "content":
                    if (!(value instanceof Map))
                        throw new InvalidConfigException(optionPath + " must be a section of Group properties");
                    content.putAll((Map<String, Object>) value);
                    break;
                case "elements":
                    elements = value;
                    break;
                case "templates":
                    if (!(value instanceof Map))
                        throw new InvalidConfigException(optionPath + " must be a section of elements");
                    for (Map.Entry<String, Object> template : ((Map<String, Object>) value).entrySet()) {
                        templates.put(template.getKey(), MenuElement.parse(template.getKey(), template.getValue(), "Group",
                                optionPath + "." + template.getKey(), new HashSet<>()));
                    }
                    break;
                default:
                    throw new InvalidConfigException(optionPath + " isn't a menu option; expected title, width, height, overlay, "
                            + "frame, lifetime, elements-per-page, content, elements or templates");
            }
        }
        if (content.containsKey("type") || content.containsKey("children"))
            throw new InvalidConfigException(path + ".content holds properties of the content Group; put elements under 'elements'");
        content.put("type", "Group");
        if (elements != null)
            content.put("children", elements);

        this.title = title;
        this.width = width;
        this.height = height;
        this.overlay = overlay;
        this.decorated = decorated;
        this.lifetime = lifetime;
        this.elementsPerPage = elementsPerPage;
        this.content = MenuElement.parse(CONTENT_KEY, content, "Group", path + ".elements", new HashSet<>());
    }

    /**
     * Replaces the title shown on the frame.
     *
     * @param title the title, formatted like any other text.
     * @return this builder.
     */
    public MenuBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Adds placeholders for the whole menu. The same as {@link #placeholders(Object...)}.
     *
     * @param placeholders the placeholders.
     * @return this builder.
     */
    public MenuBuilder globalPlaceholders(Object... placeholders) {
        return placeholders(placeholders);
    }

    /**
     * Adds a copy of a template to the end of a list element.
     *
     * @param containerKey the key of the element the row goes in.
     * @param templateKey  the key of the template under {@code templates}.
     * @return the row, to bind and fill.
     */
    public MenuRow addRow(String containerKey, String templateKey) {
        MenuElement container = require(containerKey);
        if (!MenuSchema.CONTAINERS.contains(container.type))
            throw new IllegalArgumentException(container.path + " is a " + container.type + ", which can't hold rows");
        MenuElement template = templates.get(templateKey);
        if (template == null)
            throw new IllegalArgumentException("No template '" + templateKey + "' in this menu");
        MenuRow row = new MenuRow(template);
        rows.computeIfAbsent(containerKey, k -> new ArrayList<>()).add(row);
        return row;
    }

    /**
     * Builds the menu as it stands, with the bindings, placeholders and rows added so far.
     *
     * @return the menu.
     */
    public Menu build() {
        Renderer renderer = new Renderer(Long.toString(GENERATIONS.incrementAndGet(), 36));
        renderer.frame();
        return new Menu(renderer.markup.out.toString(), renderer.markup.sets, renderer.events, renderer.actions, lifetime);
    }

    @Override
    @Nullable MenuElement element(String key) {
        return content.find(key);
    }

    @Override
    MenuBuilder self() {
        return this;
    }

    /**
     * Writes one build of the menu.
     */
    private final class Renderer {
        final MenuMarkup markup = new MenuMarkup();
        final List<Object[]> events = new ArrayList<>();
        final Map<String, ClickAction> actions = new LinkedHashMap<>();
        // Binding ids carry the build, so a click on an older build can't run an action of a newer one.
        final String generation;
        int ids;

        Renderer(String generation) {
            this.generation = generation;
        }

        String id() {
            return "K" + (++ids);
        }

        void frame() {
            if (overlay) {
                markup.open("Group", id());
                markup.raw("Background", "(Color: #000000(0.45))");
            }
            if (!decorated) {
                element(content, MenuBuilder.this, placeholders, Collections.emptyMap());
            } else {
                // The layout of Common.ui's @DecoratedContainer, which inline documents can't import.
                markup.open("Group", id());
                markup.raw("Anchor", "(Width: " + width + ", Height: " + height + ")");
                markup.open("Group", id());
                markup.raw("Anchor", "(Height: 38, Top: 0)");
                markup.raw("Background", "(TexturePath: \"Common/ContainerHeader.png\", HorizontalBorder: 50, VerticalBorder: 0)");
                markup.raw("Padding", "(Top: 7)");
                markup.open("Group", id());
                markup.raw("Anchor", "(Width: 236, Height: 11, Top: -12)");
                markup.raw("Background", "(TexturePath: \"Common/ContainerDecorationTop.png\")");
                markup.close();
                if (title != null) {
                    String titleId = id();
                    markup.open("Label", titleId);
                    markup.property(titleId, MenuSchema.LABEL_STYLE, TITLE_STYLE, "title");
                    markup.raw("Padding", "(Horizontal: 19)");
                    markup.close();
                    markup.set("#" + titleId + ".TextSpans", HytaleMessages.toMessage(title, compile(placeholders)));
                }
                markup.close();

                element(content, MenuBuilder.this, placeholders, map("LayoutMode", "Top", "Anchor", map("Top", 38),
                        "Padding", map("Full", 17), "Background", map("TexturePath", "Common/ContainerPatch.png", "Border", 23)));

                markup.open("Group", id());
                markup.raw("Anchor", "(Width: 236, Height: 11, Bottom: -6)");
                markup.raw("Background", "(TexturePath: \"Common/ContainerDecorationBottom.png\")");
                markup.close();
                markup.close();
            }
            if (overlay) {
                markup.close();
            }
        }

        void element(MenuElement element, MenuScope<?> scope, List<Object> inherited, Map<String, Object> defaults) {
            if (element.special && !scope.shown.contains(element.key))
                return;

            List<Object> placeholders = new ArrayList<>(scope.elementPlaceholders.getOrDefault(element.key, Collections.emptyList()));
            placeholders.addAll(inherited);
            PlaceholderContainer container = compile(placeholders);

            Map<String, Object> properties = new LinkedHashMap<>(defaults);
            if (element.type.equals("Title")) {
                properties.put("Style", TITLE_STYLE);
                properties.put("Padding", map("Horizontal", 19));
            } else if (element.type.equals("StyledText")) {
                properties.put("LayoutMode", "Left");
            }
            merge(properties, element.properties);
            merge(properties, scope.overrides.getOrDefault(element.key, Collections.emptyMap()));

            String id = id();
            int fontSize = 16;
            boolean bold = false;
            markup.open(markupType(element.type), id);
            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                Object value = resolve(entry.getValue(), container);
                String path = element.path + "." + entry.getKey();
                if (element.type.equals("StyledText") && entry.getKey().equals("FontSize")) {
                    fontSize = Integer.parseInt(MenuMarkup.integer(value, path));
                } else if (element.type.equals("StyledText") && entry.getKey().equals("Bold")) {
                    bold = Boolean.parseBoolean(String.valueOf(value));
                } else {
                    MenuSchema.Property property = element.property(entry.getKey());
                    if (property == null)
                        throw new IllegalStateException(path + " isn't a property of " + element.type);
                    markup.property(id, property, value, path);
                }
            }

            String text = scope.texts.getOrDefault(element.key, element.text);
            if (text != null && element.type.equals("StyledText")) {
                styledText((String) resolve(text, container), fontSize, bold);
            } else if (text != null) {
                markup.set("#" + id + ".TextSpans", HytaleMessages.toMessage(text, container));
            }

            for (MenuElement child : element.children) {
                element(child, scope, placeholders, Collections.emptyMap());
            }
            if (scope == MenuBuilder.this) {
                for (MenuRow row : rows.getOrDefault(element.key, Collections.emptyList())) {
                    List<Object> rowPlaceholders = new ArrayList<>(row.placeholders);
                    rowPlaceholders.addAll(placeholders);
                    element(row.template, row, rowPlaceholders, Collections.emptyMap());
                }
            }
            markup.close();

            MenuScope.Binding binding = scope.bindings.get(element.key);
            if (binding != null) {
                String action = generation + "." + actions.size();
                actions.put(action, binding.action);
                EventData data = EventData.of(ACTION_KEY, action);
                if (MenuSchema.VALUE_TYPES.contains(element.type))
                    data.append(VALUE_KEY, "#" + id + ".Value");
                events.add(new Object[]{binding.type, "#" + id, data});
            }
        }

        void styledText(String text, int fontSize, boolean defaultBold) {
            for (StyledTextExtractor.StyledCharacter character : StyledTextExtractor.extract(text)) {
                markup.open("Label", id());
                StringBuilder style = new StringBuilder("(TextColor: ").append(character.hexColor()).append(", FontSize: ").append(fontSize);
                if (character.bold() || defaultBold)
                    style.append(", RenderBold: true");
                if (character.italic())
                    style.append(", RenderItalics: true");
                if (!MenuSchema.isColor(character.hexColor()))
                    throw new IllegalStateException("Unexpected color " + character.hexColor());
                markup.raw("Style", style.append(')').toString());
                markup.raw("Text", MenuMarkup.quote(character.character()));
                markup.close();
            }
        }
    }

    private static String markupType(String type) {
        switch (type) {
            case "Title":
                return "Label";
            case "StyledText":
                return "Group";
            default:
                return type;
        }
    }

    /**
     * Copies the properties in, merging tuples key by key so defaults like the content's anchor survive.
     */
    @SuppressWarnings("unchecked")
    private static void merge(Map<String, Object> into, Map<String, Object> properties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Object old = into.get(entry.getKey());
            if (old instanceof Map && entry.getValue() instanceof Map && !((Map<String, Object>) entry.getValue()).containsKey("ref")) {
                Map<String, Object> merged = new LinkedHashMap<>();
                ((Map<String, Object>) old).forEach((key, value) -> merged.put(MenuSchema.pascal(key), value));
                ((Map<String, Object>) entry.getValue()).forEach((key, value) -> merged.put(MenuSchema.pascal(key), value));
                into.put(entry.getKey(), merged);
            } else {
                into.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Object resolve(Object value, PlaceholderContainer container) {
        if (value instanceof String) {
            String applied = container.applyTrusted((String) value);
            return String.join("\n", container.applyUntrusted(Collections.singletonList(applied)));
        }
        if (value instanceof Map) {
            Map<String, Object> resolved = new LinkedHashMap<>();
            ((Map<String, Object>) value).forEach((key, inner) -> resolved.put(key, resolve(inner, container)));
            return resolved;
        }
        return value;
    }

    private static PlaceholderContainer compile(List<Object> placeholders) {
        return PlaceholderContainer.compileTrusted(placeholders.toArray());
    }

    private static Map<String, Object> map(Object... pairs) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return Collections.unmodifiableMap(map);
    }

    private static int integer(Object value, String path) {
        return Integer.parseInt(MenuMarkup.integer(value, path));
    }

    private static CustomPageLifetime lifetime(String value, String path) {
        for (CustomPageLifetime lifetime : CustomPageLifetime.values()) {
            if (MenuSchema.pascal(value).equalsIgnoreCase(lifetime.name()))
                return lifetime;
        }
        throw new InvalidConfigException(path + " must be cant-close, can-dismiss or can-dismiss-or-close-through-interaction");
    }

}
