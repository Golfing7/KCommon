package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.config.exc.InvalidConfigException;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuBuilderTest {
    private static final String SHOP = String.join("\n",
            "title: '&eShop'",
            "width: 700",
            "height: 500",
            "overlay: true",
            "content:",
            "  layout-mode: Left",
            "  padding: {full: 20}",
            "elements:",
            "  items:",
            "    layout-mode: TopScrolling",
            "    scrollbar-style: '@DefaultScrollbarStyle'",
            "  footer:",
            "    type: Label",
            "    text: '&7Balance: {COINS}'",
            "    style: {font-size: 16, text-color: '#ffd700', horizontal-alignment: Center}",
            "  close:",
            "    type: TextButton",
            "    special: true",
            "    style: '@CancelTextButtonStyle'",
            "    anchor: {width: 240, height: 44}",
            "    text: Close",
            "templates:",
            "  item:",
            "    type: Button",
            "    style: {ref: '@SecondaryButtonStyle', sounds: '@ButtonSounds'}",
            "    children:",
            "      icon: {type: ItemIcon, item-id: '{ITEM}', anchor: {width: 48, height: 48}}",
            "      name: {type: Label, text: '{NAME}'}",
            "      tint: {background: '{COLOR}'}");

    private static MenuBuilder load(String yaml) {
        Map<String, Object> section = new Yaml().load(yaml);
        return new MenuBuilder(section, "menu");
    }

    private static List<String> setSelectors(Menu menu) {
        return menu.sets.stream().map(set -> (String) set[0]).collect(Collectors.toList());
    }

    private static Optional<Object> setValue(Menu menu, String selector) {
        return menu.sets.stream().filter(set -> set[0].equals(selector)).map(set -> set[1]).findFirst();
    }

    @Test
    void writesTheDecoratedFrame() {
        Menu menu = load(SHOP).build();
        MarkupParser.Node overlay = MarkupParser.parse(menu.getMarkup());

        assertEquals(Map.of("Color", "#000000(0.45)"), overlay.properties.get("Background"));
        MarkupParser.Node frame = overlay.children.get(0);
        assertEquals(Map.of("Width", "700", "Height", "500"), frame.properties.get("Anchor"));

        MarkupParser.Node titleBar = frame.children.get(0);
        MarkupParser.Node title = titleBar.children.get(1);
        assertEquals("Label", title.type);
        assertTrue(setValue(menu, "#" + title.id + ".TextSpans").orElseThrow() instanceof Message);

        MarkupParser.Node content = frame.children.get(1);
        // The content keeps the frame's anchor and texture, and takes the configured layout and padding.
        assertEquals("Left", content.properties.get("LayoutMode"));
        assertEquals(Map.of("Top", "38"), content.properties.get("Anchor"));
        assertEquals(Map.of("Full", "20"), content.properties.get("Padding"));
        assertEquals(Map.of("TexturePath", "\"Common/ContainerPatch.png\"", "Border", "23"), content.properties.get("Background"));
    }

    @Test
    void writesPropertiesAndReferences() {
        Menu menu = load(SHOP).build();
        MarkupParser.Node root = MarkupParser.parse(menu.getMarkup());
        MarkupParser.Node content = root.children.get(0).children.get(1);
        MarkupParser.Node items = content.children.get(0);
        MarkupParser.Node footer = content.children.get(1);

        assertEquals("TopScrolling", items.properties.get("LayoutMode"));
        assertFalse(items.properties.containsKey("ScrollbarStyle"), "references are sent as Set commands");
        Value<?> scrollbar = (Value<?>) setValue(menu, "#" + items.id + ".ScrollbarStyle").orElseThrow();
        assertEquals("Common.ui", scrollbar.getDocumentPath());
        assertEquals("DefaultScrollbarStyle", scrollbar.getValueName());

        assertEquals(Map.of("FontSize", "16", "TextColor", "#ffd700", "HorizontalAlignment", "Center"), footer.properties.get("Style"));
        assertTrue(setSelectors(menu).contains("#" + footer.id + ".TextSpans"));
    }

    @Test
    void specialElementsOnlyAppearWhenBound() {
        MarkupParser.Node unbound = MarkupParser.parse(load(SHOP).build().getMarkup());
        assertTrue(unbound.all().stream().noneMatch(node -> node.type.equals("TextButton")));

        AtomicInteger clicks = new AtomicInteger();
        Menu menu = load(SHOP).bindTo("close", event -> clicks.incrementAndGet()).build();
        MarkupParser.Node button = MarkupParser.parse(menu.getMarkup()).all().stream()
                .filter(node -> node.type.equals("TextButton")).findFirst().orElseThrow();
        assertEquals(Map.of("Width", "240", "Height", "44"), button.properties.get("Anchor"));

        assertEquals(1, menu.events.size());
        assertEquals("#" + button.id, menu.events.get(0)[1]);
        String action = ((EventData) menu.events.get(0)[2]).events().get(MenuBuilder.ACTION_KEY);
        assertNotNull(menu.action(action));
        menu.action(action).onClick(null);
        assertEquals(1, clicks.get());

        // Ids from another build, or made up by the client, don't resolve.
        Menu rebuilt = load(SHOP).bindTo("close", event -> { }).build();
        assertNull(rebuilt.action(action));
        assertNull(menu.action("anything"));
        assertNull(menu.action(null));
    }

    @Test
    void rowsGetTheirOwnPlaceholdersAndBindings() {
        MenuBuilder builder = load(SHOP).placeholders("COINS", "1,000");
        builder.addRow("items", "item").placeholders("NAME", "Sword", "ITEM", "Weapon_Sword", "COLOR", "#ff0000")
                .bindTo("item", event -> { });
        builder.addRow("items", "item").placeholders("NAME", "Bow \"Deluxe\"", "ITEM", "Bow\"} Group #X {", "COLOR", "#00ff00(0.5)")
                .bindTo("item", event -> { });
        Menu menu = builder.build();

        MarkupParser.Node root = MarkupParser.parse(menu.getMarkup());
        MarkupParser.Node items = root.children.get(0).children.get(1).children.get(0);
        assertEquals(2, items.children.size());

        MarkupParser.Node second = items.children.get(1);
        assertEquals("Button", second.type);
        MarkupParser.Node icon = second.children.get(0);
        assertEquals("\"Bow\"} Group #X {\"", icon.properties.get("ItemId"), "quotes in values are escaped");
        assertEquals(Map.of("Color", "#00ff00(0.5)"), second.children.get(2).properties.get("Background"));

        Value<?> style = (Value<?>) setValue(menu, "#" + second.id + ".Style").orElseThrow();
        assertEquals("SecondaryButtonStyle", style.getValueName());
        Value<?> sounds = (Value<?>) setValue(menu, "#" + second.id + ".Style.Sounds").orElseThrow();
        assertEquals("ButtonSounds", sounds.getValueName());
        List<String> selectors = setSelectors(menu);
        assertTrue(selectors.indexOf("#" + second.id + ".Style") < selectors.indexOf("#" + second.id + ".Style.Sounds"));

        assertEquals(2, menu.events.size());
        assertEquals("#" + second.id, menu.events.get(1)[1]);
    }

    @Test
    void overridesAreCheckedWhenBuilt() {
        MenuBuilder builder = load(SHOP);
        builder.addRow("items", "item").placeholders("NAME", "x", "ITEM", "y", "COLOR", "red");
        assertThrows(InvalidConfigException.class, builder::build);

        assertThrows(IllegalArgumentException.class, () -> load(SHOP).set("footer", "item-id", "x"));
        assertThrows(IllegalArgumentException.class, () -> load(SHOP).bindTo("missing", event -> { }));
        assertThrows(IllegalArgumentException.class, () -> load(SHOP).addRow("footer", "item"));
        assertThrows(IllegalArgumentException.class, () -> load(SHOP).addRow("items", "missing"));

        Menu menu = load(SHOP).disabled("close", true).show("close").build();
        MarkupParser.Node button = MarkupParser.parse(menu.getMarkup()).all().stream()
                .filter(node -> node.type.equals("TextButton")).findFirst().orElseThrow();
        assertEquals("true", button.properties.get("Disabled"));
    }

    @Test
    void rejectsMarkupTheClientWouldDisconnectOver() {
        String[] bad = {
            "elements: {a: {type: Label, colour: red}}",
            "elements: {a: {type: Label, style: {font-size: 16, text-colour: '#ffffff'}}}",
            "elements: {a: {type: Label, style: {text-color: red}}}",
            "elements: {a: {layout-mode: Sideways}}",
            "elements: {a: {type: Group, text: hi}}",
            "elements: {a: {type: Label, children: {b: {}}}}",
            "elements: {a: {type: Marquee}}",
            "elements: {a: {children: {b: {}}}, b: {}}",
            "elements: {a: {visible: '@Thing'}}",
            "elements: {a: {type: TextButton, style: {font-size: 16}}}",
            "elements: {a: {type: TextButton, style: {ref: '@X', label-style: '@Y'}}}",
            "elements: {a: {anchor: {width: 10.5}}}",
            "elements: {a: {anchor: {depth: 10}}}",
            "elements: {a: {background: red}}",
            "elemnts: {}",
            "frame: fancy",
        };
        for (String yaml : bad) {
            assertThrows(InvalidConfigException.class, () -> load(yaml), yaml);
        }
    }

    @Test
    void styledTextWritesALabelPerCharacter() {
        Menu menu = load("elements: {tag: {type: StyledText, text: '<red>A<italic>\"</italic>', font-size: 12, bold: true}}").build();
        MarkupParser.Node tag = MarkupParser.parse(menu.getMarkup()).children.get(1).children.get(0);
        assertEquals("Left", tag.properties.get("LayoutMode"));
        assertEquals(2, tag.children.size());

        @SuppressWarnings("unchecked")
        Map<String, Object> first = (Map<String, Object>) tag.children.get(0).properties.get("Style");
        assertTrue("#ff5555".equalsIgnoreCase((String) first.get("TextColor")));
        assertEquals("12", first.get("FontSize"));
        assertEquals("true", first.get("RenderBold"));
        @SuppressWarnings("unchecked")
        Map<String, Object> second = (Map<String, Object>) tag.children.get(1).properties.get("Style");
        assertEquals("true", second.get("RenderItalics"));
        assertEquals("\"\"\"", tag.children.get(1).properties.get("Text"));
    }

    @Test
    void frameNoneUsesTheContentAsTheRoot() {
        Menu menu = load("frame: none\ncontent: {anchor: {width: 300, height: 200}}\nelements: {a: {type: Title, text: Hi}}").build();
        MarkupParser.Node root = MarkupParser.parse(menu.getMarkup());
        assertEquals(Map.of("Width", "300", "Height", "200"), root.properties.get("Anchor"));
        MarkupParser.Node title = root.children.get(0);
        assertEquals("Label", title.type);
        @SuppressWarnings("unchecked")
        Map<String, Object> style = (Map<String, Object>) title.properties.get("Style");
        assertEquals("\"Secondary\"", style.get("FontName"));
    }

    @Test
    void quotesControlCharacters() {
        assertEquals("\"a b\\\"c\\\\\"", MenuMarkup.quote("a\nb\"c\\"));
    }

    @Test
    void placeholderTrustIsKept() {
        // An untrusted value is applied after formatting, so it can't inject formatting into text.
        MenuBuilder builder = load("elements: {a: {type: Label, text: 'Hi {NAME}'}}")
                .placeholders(Placeholder.curlyUntrusted("NAME", "<red>Bob"));
        Message message = (Message) builder.build().sets.get(0)[1];
        String text = rawText(message);
        assertTrue(text.contains("<red>Bob"), text);
    }

    private static String rawText(Message message) {
        StringBuilder out = new StringBuilder(message.getRawText() == null ? "" : message.getRawText());
        for (Message child : message.getChildren()) {
            out.append(rawText(child));
        }
        return out.toString();
    }
}
