package com.golfing8.kcommon.struct.helper.promise;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Only covers the promise factory/state paths that don't touch scheduling
 * (supplySync/thenApplySync/etc. resolve via {@link ThreadContext#forCurrentThread()},
 * which requires a live KCommon plugin instance that isn't bootstrapped in this
 * test environment).
 */
class PromiseTest {

    @Test
    void testCompletedPromiseIsImmediatelyDone() throws Exception {
        Promise<String> promise = Promise.completed("value");

        assertTrue(promise.isDone());
        assertFalse(promise.isCancelled());
        assertEquals("value", promise.get());
        assertEquals("value", promise.join());
        assertEquals("value", promise.getNow("fallback"));
    }

    @Test
    void testExceptionallyPromisePropagatesFailure() {
        RuntimeException cause = new RuntimeException("boom");
        Promise<String> promise = Promise.exceptionally(cause);

        assertTrue(promise.isDone());
        ExecutionException thrown = assertThrows(ExecutionException.class, promise::get);
        assertSame(cause, thrown.getCause());
    }

    @Test
    void testEmptyPromiseCanBeSuppliedSynchronouslyByValue() throws Exception {
        Promise<String> promise = Promise.empty();
        assertFalse(promise.isDone());

        promise.supply("later value");

        assertTrue(promise.isDone());
        assertEquals("later value", promise.get());
    }

    @Test
    void testEmptyPromiseCanBeSuppliedWithException() {
        Promise<String> promise = Promise.empty();
        promise.supplyException(new IllegalStateException("nope"));

        assertTrue(promise.isDone());
        ExecutionException thrown = assertThrows(ExecutionException.class, promise::get);
        assertEquals("nope", thrown.getCause().getMessage());
    }

    @Test
    void testCancelMarksPromiseClosedAndCancelled() throws Exception {
        Promise<String> promise = Promise.empty();

        assertFalse(promise.isClosed());
        promise.close();

        assertTrue(promise.isCancelled());
        assertTrue(promise.isClosed());
        assertThrows(CancellationException.class, promise::join);
    }

    @Test
    void testStartReturnsAlreadyCompletedVoidPromise() throws Exception {
        Promise<Void> promise = Promise.start();
        assertTrue(promise.isDone());
        assertNull(promise.get());
    }

    @Test
    void testWrapFutureOfAlreadyCompletedCompletableFuture() throws Exception {
        CompletableFuture<String> future = CompletableFuture.completedFuture("wrapped");
        Promise<String> promise = Promise.wrapFuture(future);

        assertTrue(promise.isDone());
        assertEquals("wrapped", promise.get());
    }

    @Test
    void testToCompletableFutureReflectsCompletedValue() {
        Promise<String> promise = Promise.completed("done");
        CompletableFuture<String> future = promise.toCompletableFuture();

        assertTrue(future.isDone());
        assertEquals("done", future.getNow(null));
    }
}
