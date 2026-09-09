package com.golfing8.kcommon;

import com.golfing8.kcommon.module.test.util.FakeServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link ComponentUtils}, the low-level string -&gt; {@link Component} parsing pipeline
 * that sits underneath {@code MS}. These tests exist as a regression net for changes made while
 * optimizing the parsing pipeline (see AGENTS.md profiling work) - they assert on rendered/legacy
 * output rather than on the intermediate MiniMessage tag strings so that internal reshuffling of
 * {@code processLine}/{@code applyTransformers} doesn't break them as long as behavior is preserved.
 */
class ComponentUtilsTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    /**
     * Serializes a component back to a legacy '&amp;'-coded string for easy comparison against
     * {@link ChatColor#translateAlternateColorCodes(char, String)} expectations.
     */
    private static String legacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component).replace(ChatColor.COLOR_CHAR, '&');
    }

    private static String plain(Component component) {
        return ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(component));
    }

    private static TextColor firstColor(Component component) {
        Component target = component.children().isEmpty() ? component : component.children().get(0);
        return target.color();
    }

    @Nested
    @DisplayName("toComponent(String)")
    class ToComponentSingle {
        @Test
        @DisplayName("null input returns null")
        void testNull() {
            assertNull(ComponentUtils.toComponent((String) null));
        }

        @Test
        @DisplayName("Plain text with no formatting round-trips unchanged")
        void testPlainText() {
            Component component = ComponentUtils.toComponent("Hello World");
            assertEquals("Hello World", plain(component));
        }

        @Test
        @DisplayName("Ampersand legacy color codes are translated")
        void testAmpersandLegacyColor() {
            Component component = ComponentUtils.toComponent("&aGreen &cRed");
            assertEquals("&aGreen &cRed", legacy(component));
        }

        @Test
        @DisplayName("Section-sign legacy color codes are translated")
        void testSectionLegacyColor() {
            Component component = ComponentUtils.toComponent(ChatColor.translateAlternateColorCodes('&', "&aGreen &cRed"));
            assertEquals("&aGreen &cRed", legacy(component));
        }

        @Test
        @DisplayName("Formatting codes (bold/underline) are preserved")
        void testFormattingCodes() {
            Component component = ComponentUtils.toComponent("&lBold &nUnderline");
            assertEquals("&lBold &nUnderline", legacy(component));
        }

        @Test
        @DisplayName("Components default to italic=false when not explicitly set")
        void testDefaultsNonItalic() {
            Component component = ComponentUtils.toComponent("plain text");
            assertEquals(TextDecoration.State.FALSE, component.decoration(TextDecoration.ITALIC));
        }

        @Test
        @DisplayName("Explicit <italic> tag is honored rather than forced off")
        void testExplicitItalicHonored() {
            Component component = ComponentUtils.toComponent("<italic>slanted");
            assertEquals(TextDecoration.State.TRUE, component.decoration(TextDecoration.ITALIC));
        }

        @Test
        @DisplayName("String escapes (\\n etc) are unescaped")
        void testUnescapesJavaEscapes() {
            Component component = ComponentUtils.toComponent("Line1\\nLine2");
            assertEquals("Line1\nLine2", plain(component));
        }

        @Test
        @DisplayName("Mixed '&' and '§' legacy codes in the same string both resolve correctly")
        void testMixedAmpersandAndSectionColors() {
            Component component = ComponentUtils.toComponent("&aGreen " + ChatColor.COLOR_CHAR + "cRed");
            assertEquals("&aGreen &cRed", legacy(component));
        }
    }

    @Nested
    @DisplayName("toComponent(String) caching")
    class Caching {
        @Test
        @DisplayName("Parsing the same string twice returns the same cached Component instance")
        void testRepeatedCallsShareInstance() {
            String message = "&aCache me: " + System.nanoTime();
            Component first = ComponentUtils.toComponent(message);
            Component second = ComponentUtils.toComponent(message);
            assertSame(first, second);
        }

        @Test
        @DisplayName("Different strings never share a cached instance")
        void testDifferentStringsDoNotShareInstance() {
            Component a = ComponentUtils.toComponent("&aDistinct A: " + System.nanoTime());
            Component b = ComponentUtils.toComponent("&aDistinct B: " + System.nanoTime());
            assertNotSame(a, b);
        }
    }

    @Nested
    @DisplayName("toComponent(String) hex colors")
    class HexColors {
        @Test
        @DisplayName("6-digit ampersand hex (&#RRGGBB) resolves to the correct TextColor")
        void testAmpersand6Digit() {
            Component component = ComponentUtils.toComponent("&#FF0000Text");
            assertEquals(TextColor.fromHexString("#FF0000"), firstColor(component));
            assertEquals("Text", plain(component));
        }

        @Test
        @DisplayName("3-digit ampersand hex (&#RGB) expands each digit and resolves correctly")
        void testAmpersand3Digit() {
            Component component = ComponentUtils.toComponent("&#0F0Text");
            assertEquals(TextColor.fromHexString("#00FF00"), firstColor(component));
        }

        @Test
        @DisplayName("Section-sign hex (§#RRGGBB) resolves to the correct TextColor")
        void testSectionHex() {
            Component component = ComponentUtils.toComponent("§#00FF00Text");
            assertEquals(TextColor.fromHexString("#00FF00"), firstColor(component));
        }

        @Test
        @DisplayName("Split spigot hex format (&x&R&R&G&G&B&B) resolves to the correct TextColor")
        void testSpigotSplitHex() {
            Component component = ComponentUtils.toComponent("&x&F&F&0&0&0&0Text");
            assertEquals(TextColor.fromHexString("#FF0000"), firstColor(component));
        }

        @Test
        @DisplayName("Bracketed hex format ({&#RRGGBB}) resolves to the correct TextColor")
        void testBracketedHex() {
            Component component = ComponentUtils.toComponent("{&#00FF00}Text");
            assertEquals(TextColor.fromHexString("#00FF00"), firstColor(component));
        }
    }

    @Nested
    @DisplayName("toFlatComponent(List)")
    class ToFlatComponent {
        @Test
        @DisplayName("Empty list produces an empty component")
        void testEmptyList() {
            assertEquals(Component.empty(), ComponentUtils.toFlatComponent(Collections.emptyList()));
        }

        @Test
        @DisplayName("Multiple lines are joined with newlines, preserving each line's formatting")
        void testJoinsWithNewlines() {
            Component component = ComponentUtils.toFlatComponent(Arrays.asList("Line1", "&cLine2"));
            assertEquals("Line1\n&cLine2", legacy(component));
        }

        @Test
        @DisplayName("Single-element list produces the same result as toComponent(String)")
        void testSingleElementMatchesSingleString() {
            Component flat = ComponentUtils.toFlatComponent(Collections.singletonList("&aHi"));
            Component single = ComponentUtils.toComponent("&aHi");
            assertEquals(legacy(single), legacy(flat));
        }
    }

    @Nested
    @DisplayName("toComponent(List)")
    class ToComponentList {
        @Test
        @DisplayName("Empty list produces an empty list")
        void testEmptyList() {
            assertEquals(Collections.emptyList(), ComponentUtils.toComponent(Collections.emptyList()));
        }

        @Test
        @DisplayName("Each line is parsed independently, order preserved")
        void testEachLineParsedIndependently() {
            List<Component> components = ComponentUtils.toComponent(Arrays.asList("&aOne", "&cTwo"));
            assertEquals(2, components.size());
            assertEquals("&aOne", legacy(components.get(0)));
            assertEquals("&cTwo", legacy(components.get(1)));
        }
    }

    @Nested
    @DisplayName("maybeCenter")
    class Centering {
        @Test
        @DisplayName("Strings without the center:: prefix are unchanged")
        void testNoPrefixUnchanged() {
            assertEquals("Hello World", ComponentUtils.maybeCenter("Hello World"));
        }

        @Test
        @DisplayName("center:: prefix is stripped and leading padding is added")
        void testCenterPrefixPadded() {
            String result = ComponentUtils.maybeCenter("center::Hi");
            assertTrue(result.endsWith("Hi"));
            assertTrue(result.length() > "Hi".length());
            assertTrue(result.startsWith(" "));
        }
    }
}
