package com.golfing8.kcommon.struct.helper.exception;

import com.golfing8.kcommon.struct.helper.exception.types.SchedulerTaskException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InternalExceptionTest {

    @Test
    void testMessageIncludesWhatAndPreservesCause() {
        RuntimeException cause = new RuntimeException("root cause");
        SchedulerTaskException exception = new SchedulerTaskException(cause);

        assertTrue(exception.getMessage().contains("scheduler task"));
        assertSame(cause, exception.getCause());
    }
}
