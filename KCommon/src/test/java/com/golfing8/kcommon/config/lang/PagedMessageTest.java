package com.golfing8.kcommon.config.lang;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PagedMessageTest {

    @Test
    @DisplayName("Partitions messages into pages of the given size")
    void testPagination() {
        List<String> lines = Arrays.asList("1", "2", "3", "4", "5");
        PagedMessage paged = new PagedMessage(lines, 2, "HEADER", "FOOTER");

        assertEquals(3, paged.getTotalPages());
        assertEquals(Arrays.asList("1", "2"), paged.getPagedMessages().get(0));
        assertEquals(Arrays.asList("3", "4"), paged.getPagedMessages().get(1));
        assertEquals(Collections.singletonList("5"), paged.getPagedMessages().get(2));
    }

    @Test
    @DisplayName("Falls back to a placeholder line when there are no messages")
    void testEmptyMessagesFallback() {
        PagedMessage paged = new PagedMessage(Collections.emptyList(), 5, "H", "F");
        assertEquals(1, paged.getParsedMessages().size());
    }

    @Test
    @DisplayName("Every instance has a unique, non-null ID")
    void testUniqueId() {
        PagedMessage first = new PagedMessage(Collections.singletonList("a"), 5, "H", "F");
        PagedMessage second = new PagedMessage(Collections.singletonList("a"), 5, "H", "F");
        assertNotNull(first.getId());
        org.junit.jupiter.api.Assertions.assertNotEquals(first.getId(), second.getId());
    }
}
