package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.module.test.util.FakeServer;
import com.golfing8.kcommon.struct.title.Title;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    @Nested
    @DisplayName("Constructing from raw values")
    class ConstructFromRaw {
        @Test
        @DisplayName("From a single string, produces a one-line message")
        void testFromString() {
            Message message = new Message("Hello World");
            assertEquals(Collections.singletonList("Hello World"), message.getMessages());
            assertTrue(message.isSimple());
        }

        @Test
        @DisplayName("From a list, preserves every line")
        void testFromList() {
            Message message = new Message(Arrays.asList("Line 1", "Line 2"));
            assertEquals(Arrays.asList("Line 1", "Line 2"), message.getMessages());
        }

        @Test
        @DisplayName("From null, produces an empty message")
        void testFromNull() {
            Message message = new Message((Object) null);
            assertNull(message.getMessages());
            assertTrue(message.isEmpty());
        }

        @Test
        @DisplayName("Throws for an unsupported source type")
        void testFromUnsupportedType() {
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> new Message(42));
        }
    }

    @Nested
    @DisplayName("Constructing from a map")
    class ConstructFromMap {
        @Test
        @DisplayName("Reads message, actionbar, and paging options from a map")
        void testFromMap() {
            Map<String, Object> section = new HashMap<>();
            section.put("message", Arrays.asList("line1", "line2"));
            section.put("actionbar", "action!");
            section.put("paged", true);
            section.put("page-height", 5);
            section.put("page-header", "HEADER");
            section.put("page-footer", "FOOTER");

            Message message = new Message(section);
            assertEquals(Arrays.asList("line1", "line2"), message.getMessages());
            assertEquals("action!", message.getActionBar());
            assertTrue(message.isPaged());
            assertEquals(5, message.getPageHeight());
            assertEquals("HEADER", message.getPageHeader());
            assertEquals("FOOTER", message.getPageFooter());
            assertFalse(message.isSimple());
        }

        @Test
        @DisplayName("A message with only a single-line string message is still simple")
        void testSimpleFromMap() {
            Map<String, Object> section = new HashMap<>();
            section.put("message", "just text");
            Message message = new Message(section);
            assertTrue(message.isSimple());
        }
    }

    @Nested
    @DisplayName("Copy construction")
    class CopyConstruction {
        @Test
        @DisplayName("Copies all fields from another MessageContainer")
        void testCopyConstructor() {
            Message original = new Message(Collections.singletonList("hi"), null,
                    new Title("T", "S", 1, 2, 3), "bar");
            Message copy = new Message(original);
            assertEquals(original.getMessages(), copy.getMessages());
            assertEquals(original.getTitle(), copy.getTitle());
            assertEquals(original.getActionBar(), copy.getActionBar());
        }
    }

    @Nested
    @DisplayName("isSimple / isEmpty")
    class SimpleAndEmpty {
        @Test
        @DisplayName("A message with a title is not simple")
        void testTitleMakesNotSimple() {
            Message message = new Message(Collections.singletonList("hi"), null, new Title("T", null, 1, 2, 3), null);
            assertFalse(message.isSimple());
        }

        @Test
        @DisplayName("An empty message with no lines is empty")
        void testEmptyMessage() {
            Message message = Message.builder().build();
            assertTrue(message.isEmpty());
        }

        @Test
        @DisplayName("A non-simple message with content is not considered empty")
        void testNonSimpleIsNotEmpty() {
            Message message = new Message(null, null, new Title("T", null, 1, 2, 3), null);
            assertFalse(message.isEmpty());
        }
    }

    @Test
    @DisplayName("Round trips through a YAML-backed configuration section")
    void testFromConfigurationSection() throws Exception {
        String yaml =
                "test-message:\n" +
                        "  message:\n" +
                        "    - 'line1'\n" +
                        "    - 'line2'\n" +
                        "  actionbar: 'bar text'\n";
        Configuration configuration = new Configuration(Paths.get(getClass().getSimpleName() + "_testFromConfigurationSection.yml"));
        configuration.loadFromString(yaml);

        Message message = new Message(configuration.getConfigurationSection("test-message"));
        assertEquals(Arrays.asList("line1", "line2"), message.getMessages());
        assertEquals("bar text", message.getActionBar());
    }
}
