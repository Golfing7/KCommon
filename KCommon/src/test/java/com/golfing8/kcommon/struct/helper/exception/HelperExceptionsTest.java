package com.golfing8.kcommon.struct.helper.exception;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Only covers the non-throwing path of {@link HelperExceptions#wrapSchedulerTask}.
 * The exception-reporting path calls {@code KCommon.getInstance()}, which is only
 * populated by a full plugin onEnable lifecycle that isn't bootstrapped in this
 * test environment, so it's skipped here.
 */
class HelperExceptionsTest {

    @Test
    void testWrapSchedulerTaskRunsDelegateOnSuccess() {
        AtomicBoolean ran = new AtomicBoolean(false);
        Runnable wrapped = HelperExceptions.wrapSchedulerTask(() -> ran.set(true));
        wrapped.run();
        assertTrue(ran.get());
    }
}
