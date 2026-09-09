package com.golfing8.kcommon.util;

import com.golfing8.kcommon.module.test.util.FakeServer;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import net.kyori.adventure.text.Component;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link MS}, the primary entry point plugins use to turn raw strings (with legacy color
 * codes, hex codes, macros, and placeholders) into rendered {@link Component}s. These tests are a
 * regression net for the parsing-pipeline optimization work tracked alongside AGENTS.md - they
 * assert on final rendered output (legacy-serialized text/colors), not on which internal
 * transformer produced it, so the pipeline can be restructured without breaking them.
 * <p>
 * Delivery methods ({@code sendActionBar}, {@code sendTitle}, {@code pass}) are intentionally not
 * covered here: they require a fully functional {@code BukkitAudiences} bound to a real
 * NMS-version-matched {@code Player}, which is out of scope for the fake 1.8 test server used
 * elsewhere in this module.
 */
class MSTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    private static String legacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component).replace(ChatColor.COLOR_CHAR, '&');
    }

    private static String plain(Component component) {
        return ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(component));
    }

    @Nested
    @DisplayName("applyTransformers")
    class ApplyTransformers {
        @Test
        @DisplayName("Translates ampersand legacy color codes")
        void testLegacyColor() {
            assertEquals(ChatColor.translateAlternateColorCodes('&', "&aHello"), MS.applyTransformers("&aHello"));
        }

        @Test
        @DisplayName("Translates hex color codes")
        void testHexColor() {
            String result = MS.applyTransformers("&#FF0000Text");
            assertTrue(result.contains("Text"));
            assertEquals(RGBUtils.INSTANCE.hexColor(ChatColor.translateAlternateColorCodes('&', "&#FF0000Text")), result);
        }

        @Test
        @DisplayName("Runs string macros")
        void testMacros() {
            assertEquals("1,000,000", MS.applyTransformers("$commas{1000000}"));
        }

        @Test
        @DisplayName("Chains color translation and macros in the same string")
        void testColorAndMacroChained() {
            String result = MS.applyTransformers("&aBalance: $commas{1000000}");
            assertEquals(ChatColor.translateAlternateColorCodes('&', "&aBalance: 1,000,000"), result);
        }
    }

    @Nested
    @DisplayName("parseSingle")
    class ParseSingle {
        @Test
        @DisplayName("null input returns null")
        void testNull() {
            assertNull(MS.parseSingle(null));
        }

        @Test
        @DisplayName("Substitutes trusted placeholders before running transformers")
        void testTrustedPlaceholderThenTransform() {
            String result = MS.parseSingle("&a{NAME}: $commas{1000000}", "NAME", "Bal");
            assertEquals(ChatColor.translateAlternateColorCodes('&', "&aBal: 1,000,000"), result);
        }
    }

    @Nested
    @DisplayName("parseAll")
    class ParseAll {
        @Test
        @DisplayName("Parses every line independently")
        void testParsesEveryLine() {
            List<String> result = MS.parseAll(Arrays.asList("&aLine1", "&cLine2"));
            assertEquals(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "&aLine1"),
                    ChatColor.translateAlternateColorCodes('&', "&cLine2")
            ), result);
        }

        @Test
        @DisplayName("A trusted MultiLinePlaceholder expands one line into several before transforming")
        void testMultiLinePlaceholderExpansion() {
            MultiLinePlaceholder placeholder = MultiLinePlaceholder.percentTrusted("LORE", Arrays.asList("&aFirst", "&cSecond"));
            List<String> result = MS.parseAll(Collections.singletonList("%LORE%"), placeholder);
            assertEquals(Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "&aFirst"),
                    ChatColor.translateAlternateColorCodes('&', "&cSecond")
            ), result);
        }
    }

    @Nested
    @DisplayName("toComponent(String, Object...)")
    class ToComponentSingle {
        @Test
        @DisplayName("null/empty message returns an empty component")
        void testEmptyMessage() {
            assertEquals(Component.empty(), MS.toComponent((String) null));
            assertEquals(Component.empty(), MS.toComponent(""));
        }

        @Test
        @DisplayName("Renders legacy colors and macros end to end")
        void testFullPipeline() {
            Component component = MS.toComponent("&aBalance: $commas{1000000}");
            assertEquals("&aBalance: 1,000,000", legacy(component));
        }

        @Test
        @DisplayName("Trusted placeholder values are parsed for color/formatting")
        void testTrustedPlaceholderIsColorParsed() {
            Component component = MS.toComponent("Hi {NAME}", "NAME", "&cRed");
            assertEquals("Hi &cRed", legacy(component));
        }

        @Test
        @DisplayName("Trusted placeholder values are color-parsed into a real color, not literal text")
        void testTrustedPlaceholderProducesRealColor() {
            Placeholder trusted = Placeholder.curlyTrustedArg("NAME", "&cRed", true);
            String raw = LegacyComponentSerializer.legacySection().serialize(MS.toComponent("Hi {NAME}", trusted));
            // The ampersand is consumed by parsing and becomes a real color escape, so no literal '&' remains.
            assertTrue(raw.contains(ChatColor.COLOR_CHAR + "cRed"));
            assertTrue(raw.indexOf('&') == -1);
        }

        @Test
        @DisplayName("Untrusted placeholder values are inserted as literal text, not color-parsed")
        void testUntrustedPlaceholderIsLiteral() {
            Placeholder untrusted = Placeholder.curlyTrustedArg("NAME", "&cRed", false);
            String raw = LegacyComponentSerializer.legacySection().serialize(MS.toComponent("Hi {NAME}", untrusted));
            // No real color escape is produced - the literal characters '&', 'c', 'R', 'e', 'd' pass through untouched.
            assertTrue(raw.contains("&cRed"));
            assertTrue(raw.indexOf(ChatColor.COLOR_CHAR) == -1);
        }

        @Test
        @DisplayName("String placeholder keys are matched case-insensitively via uppercasing")
        void testPlaceholderKeyUppercased() {
            Component component = MS.toComponent("Hi {NAME}", "name", "World");
            assertEquals("Hi World", plain(component));
        }
    }

    @Nested
    @DisplayName("toComponent(List, Object...)")
    class ToComponentMultiLine {
        @Test
        @DisplayName("null/empty list returns an empty component")
        void testEmptyList() {
            assertEquals(Component.empty(), MS.toComponent((List<String>) null));
            assertEquals(Component.empty(), MS.toComponent(Collections.emptyList()));
        }

        @Test
        @DisplayName("Lines are joined with newlines and each line is fully parsed")
        void testJoinsLines() {
            Component component = MS.toComponent(Arrays.asList("&aLine1 {A}", "&cLine2 {B}"), "A", "1", "B", "2");
            assertEquals("&aLine1 1\n&cLine2 2", legacy(component));
        }
    }

    @Nested
    @DisplayName("toComponentList(List, Object...)")
    class ToComponentListTest {
        @Test
        @DisplayName("null/empty list returns an empty list")
        void testEmptyList() {
            assertEquals(Collections.emptyList(), MS.toComponentList(null));
            assertEquals(Collections.emptyList(), MS.toComponentList(Collections.emptyList()));
        }

        @Test
        @DisplayName("Each line becomes its own parsed Component, order preserved")
        void testEachLineOwnComponent() {
            List<Component> components = MS.toComponentList(Arrays.asList("&aOne {X}", "&cTwo"), "X", "1");
            assertEquals(2, components.size());
            assertEquals("&aOne 1", legacy(components.get(0)));
            assertEquals("&cTwo", legacy(components.get(1)));
        }
    }
}
