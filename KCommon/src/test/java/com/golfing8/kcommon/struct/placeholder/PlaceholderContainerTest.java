package com.golfing8.kcommon.struct.placeholder;

import com.golfing8.kcommon.module.test.util.FakeServer;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers {@link PlaceholderContainer}, which sits between {@code MS} and raw placeholder objects,
 * compiling varargs into trusted/untrusted {@link Placeholder}/{@link MultiLinePlaceholder} lists
 * and applying them to strings and components.
 */
class PlaceholderContainerTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    private static String legacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component).replace(ChatColor.COLOR_CHAR, '&');
    }

    @Nested
    @DisplayName("compileTrusted")
    class Compile {
        @Test
        @DisplayName("No arguments returns the shared EMPTY instance")
        void testNoArgsReturnsEmpty() {
            assertSame(PlaceholderContainer.EMPTY, PlaceholderContainer.compileTrusted());
        }

        @Test
        @DisplayName("A single existing PlaceholderContainer argument is passed through, not re-cloned")
        void testSingleContainerPassthrough() {
            PlaceholderContainer container = PlaceholderContainer.compileTrusted("KEY", "value");
            assertSame(container, PlaceholderContainer.compileTrusted((Object) container));
        }

        @Test
        @DisplayName("String key/value pairs become uppercase-bracketed trusted placeholders")
        void testKeyValuePairsCompiled() {
            PlaceholderContainer container = PlaceholderContainer.compileTrusted("name", "World");
            assertEquals(1, container.getPlaceholders().size());
            Placeholder placeholder = container.getPlaceholders().get(0);
            assertEquals("{NAME}", placeholder.getLabel());
            assertEquals("World", placeholder.getValue());
            assertTrue(placeholder.isTrusted());
        }

        @Test
        @DisplayName("A List value for a key compiles to a trusted MultiLinePlaceholder instead")
        void testListValueCompilesMultiLine() {
            PlaceholderContainer container = PlaceholderContainer.compileTrusted("lore", Arrays.asList("A", "B"));
            assertEquals(0, container.getPlaceholders().size());
            assertEquals(1, container.getMultiLinePlaceholders().size());
            assertEquals("%LORE%", container.getMultiLinePlaceholders().get(0).getLabel());
            assertEquals(Arrays.asList("A", "B"), container.getMultiLinePlaceholders().get(0).getReplacement());
        }

        @Test
        @DisplayName("Existing Placeholder objects are passed through with their own trusted flag intact")
        void testPlaceholderObjectPassthrough() {
            Placeholder untrusted = Placeholder.curlyTrustedArg("NAME", "value", false);
            PlaceholderContainer container = PlaceholderContainer.compileTrusted(untrusted);
            assertEquals(1, container.getPlaceholders().size());
            assertTrue(!container.getPlaceholders().get(0).isTrusted());
        }

        @Test
        @DisplayName("Nested arrays and collections are flattened")
        void testFlattensNestedArraysAndCollections() {
            PlaceholderContainer container = PlaceholderContainer.compileTrusted(
                    (Object) new Object[]{"A", "1"},
                    Collections.singletonList(Placeholder.curlyTrustedArg("B", "2", true))
            );
            assertEquals(2, container.getPlaceholders().size());
        }

        @Test
        @DisplayName("A trailing key with no matching value throws")
        void testUnbalancedThrows() {
            assertThrows(IllegalArgumentException.class, () -> PlaceholderContainer.compileTrusted("KEY"));
        }
    }

    @Nested
    @DisplayName("applyTrusted / applyUntrusted (List<String>)")
    class ApplyToStrings {
        @Test
        @DisplayName("applyTrusted substitutes trusted placeholders only")
        void testAppliesOnlyTrusted() {
            Placeholder trusted = Placeholder.curlyTrustedArg("A", "1", true);
            Placeholder untrusted = Placeholder.curlyTrustedArg("B", "2", false);
            PlaceholderContainer container = new PlaceholderContainer(Arrays.asList(trusted, untrusted), Collections.emptyList());

            List<String> result = container.applyTrusted(Collections.singletonList("{A} {B}"));
            assertEquals(Collections.singletonList("1 {B}"), result);
        }

        @Test
        @DisplayName("applyUntrusted substitutes untrusted placeholders only")
        void testAppliesOnlyUntrusted() {
            Placeholder trusted = Placeholder.curlyTrustedArg("A", "1", true);
            Placeholder untrusted = Placeholder.curlyTrustedArg("B", "2", false);
            PlaceholderContainer container = new PlaceholderContainer(Arrays.asList(trusted, untrusted), Collections.emptyList());

            List<String> result = container.applyUntrusted(Collections.singletonList("{A} {B}"));
            assertEquals(Collections.singletonList("{A} 2"), result);
        }

        @Test
        @DisplayName("A trusted MultiLinePlaceholder expands a matching line into several lines")
        void testMultiLineExpansion() {
            MultiLinePlaceholder placeholder = MultiLinePlaceholder.percentTrusted("LORE", Arrays.asList("A", "B", "C"));
            PlaceholderContainer container = new PlaceholderContainer(Collections.emptyList(), Collections.singletonList(placeholder));

            List<String> result = container.applyTrusted(Arrays.asList("before", "%LORE%", "after"));
            assertEquals(Arrays.asList("before", "A", "B", "C", "after"), result);
        }

        @Test
        @DisplayName("A trusted MultiLinePlaceholder with no matching line leaves input unchanged")
        void testMultiLineNoMatch() {
            MultiLinePlaceholder placeholder = MultiLinePlaceholder.percentTrusted("LORE", Arrays.asList("A", "B"));
            PlaceholderContainer container = new PlaceholderContainer(Collections.emptyList(), Collections.singletonList(placeholder));

            List<String> result = container.applyTrusted(Collections.singletonList("no placeholder here"));
            assertEquals(Collections.singletonList("no placeholder here"), result);
        }
    }

    @Nested
    @DisplayName("applyUntrusted(Component)")
    class ApplyToComponent {
        @Test
        @DisplayName("Untrusted placeholder text is inserted literally into the component")
        void testUntrustedTextInsertedLiterally() {
            Placeholder untrusted = Placeholder.curlyTrustedArg("NAME", "&cRed", false);
            PlaceholderContainer container = new PlaceholderContainer(Collections.singletonList(untrusted), Collections.emptyList());

            Component base = Component.text("Hi {NAME}");
            Component result = container.applyUntrusted(base);
            String raw = LegacyComponentSerializer.legacySection().serialize(result);
            assertTrue(raw.contains("&cRed"));
            assertTrue(raw.indexOf(ChatColor.COLOR_CHAR) == -1);
        }

        @Test
        @DisplayName("Trusted placeholders are ignored by applyUntrusted(Component)")
        void testTrustedIgnored() {
            Placeholder trusted = Placeholder.curlyTrustedArg("NAME", "World", true);
            PlaceholderContainer container = new PlaceholderContainer(Collections.singletonList(trusted), Collections.emptyList());

            Component base = Component.text("Hi {NAME}");
            Component result = container.applyUntrusted(base);
            assertEquals("Hi {NAME}", legacy(result));
        }

        @Test
        @DisplayName("An untrusted MultiLinePlaceholder expands into newline-joined text within the component")
        void testUntrustedMultiLineJoinedWithNewlines() {
            MultiLinePlaceholder placeholder = MultiLinePlaceholder.percentUntrusted("LORE", Arrays.asList("A", "B"));
            PlaceholderContainer container = new PlaceholderContainer(Collections.emptyList(), Collections.singletonList(placeholder));

            Component base = Component.text("%LORE%");
            Component result = container.applyUntrusted(base);
            assertEquals("A\nB", legacy(result));
        }
    }
}
