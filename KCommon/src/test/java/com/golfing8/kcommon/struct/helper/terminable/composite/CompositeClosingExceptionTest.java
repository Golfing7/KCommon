package com.golfing8.kcommon.struct.helper.terminable.composite;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompositeClosingExceptionTest {

    @Test
    void testMessageContainsCauses() {
        RuntimeException cause = new RuntimeException("failed");
        CompositeClosingException exception = new CompositeClosingException(Collections.singletonList(cause));

        assertTrue(exception.getMessage().contains("failed"));
        assertTrue(exception.getCauses().contains(cause));
    }

    @Test
    void testRejectsEmptyCauseList() {
        assertThrows(IllegalArgumentException.class, () -> new CompositeClosingException(Collections.emptyList()));
    }
}
