package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.module.test.util.FakeServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageContainerTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    @Nested
    @DisplayName("append")
    class Append {
        @Test
        @DisplayName("END_OF_LINE mode joins corresponding lines with the separator")
        void testAppendEndOfLine() {
            Message first = new Message(Arrays.asList("a1", "a2"));
            Message second = new Message(Arrays.asList("b1", "b2"));
            Message result = first.append(second, "-");
            assertEquals(Arrays.asList("a1-b1", "a2-b2"), result.getMessages());
        }

        @Test
        @DisplayName("AFTER mode concatenates all lines from the first message, then the second")
        void testAppendAfter() {
            Message first = new Message(Arrays.asList("a1", "a2"));
            Message second = new Message(Arrays.asList("b1", "b2"));
            Message result = first.append(second, Message.AppendMode.AFTER, null);
            assertEquals(Arrays.asList("a1", "a2", "b1", "b2"), result.getMessages());
        }

        @Test
        @DisplayName("INTERLEAVE mode alternates lines from each message")
        void testAppendInterleave() {
            Message first = new Message(Arrays.asList("a1", "a2"));
            Message second = new Message(Collections.singletonList("b1"));
            Message result = first.append(second, Message.AppendMode.INTERLEAVE, null);
            assertEquals(Arrays.asList("a1", "b1", "a2"), result.getMessages());
        }

        @Test
        @DisplayName("Appending null returns a copy of the original message")
        void testAppendNull() {
            Message first = new Message(Collections.singletonList("a1"));
            Message result = first.append(null);
            assertEquals(first.getMessages(), result.getMessages());
        }
    }

    @Nested
    @DisplayName("appendMessages")
    class AppendMessagesOnly {
        @Test
        @DisplayName("Uses the other message's lines when this message has none")
        void testThisNullUsesOther() {
            Message empty = new Message((java.util.List<String>) null, null, null, null);
            Message other = new Message(Collections.singletonList("only"));
            java.util.List<String> result = empty.appendMessages(other, Message.AppendMode.AFTER, null);
            assertEquals(Collections.singletonList("only"), result);
        }
    }

    @Test
    @DisplayName("toPagedMessage parses placeholders and wraps as a PagedMessage")
    void testToPagedMessage() {
        Message message = new Message(Arrays.asList("Hello {NAME}"));
        PagedMessage paged = message.toPagedMessage("NAME", "World");
        assertEquals(Collections.singletonList("Hello World"), paged.getParsedMessages());
    }
}
