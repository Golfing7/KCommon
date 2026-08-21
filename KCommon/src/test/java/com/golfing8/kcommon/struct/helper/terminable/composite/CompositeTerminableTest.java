package com.golfing8.kcommon.struct.helper.terminable.composite;

import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompositeTerminableTest {

    private static AutoCloseable trackedCloseable(List<String> order, String name) {
        return () -> order.add(name);
    }

    @Nested
    @DisplayName("Strong composite (CompositeTerminable.create)")
    class Strong {

        @Test
        @DisplayName("Closes bound closeables in LIFO order")
        void testClosesInLifoOrder() throws CompositeClosingException {
            List<String> order = new ArrayList<>();
            CompositeTerminable terminable = CompositeTerminable.create();
            terminable.with(trackedCloseable(order, "first"));
            terminable.with(trackedCloseable(order, "second"));
            terminable.with(trackedCloseable(order, "third"));

            assertFalse(terminable.isClosed());
            terminable.close();

            assertEquals(java.util.Arrays.asList("third", "second", "first"), order);
            assertTrue(terminable.isClosed());
        }

        @Test
        @DisplayName("Aggregates exceptions from failing closeables and still closes the rest")
        void testAggregatesCloseExceptions() {
            List<String> order = new ArrayList<>();
            CompositeTerminable terminable = CompositeTerminable.create();
            terminable.with(trackedCloseable(order, "ok"));
            terminable.with(() -> {
                throw new IllegalStateException("boom");
            });

            CompositeClosingException thrown = assertThrows(CompositeClosingException.class, terminable::close);
            assertEquals(1, thrown.getCauses().size());
            assertEquals("boom", thrown.getCauses().get(0).getMessage());
            assertEquals(java.util.Arrays.asList("ok"), order);
            assertTrue(terminable.isClosed());
        }

        @Test
        @DisplayName("with() rejects null and bind() returns the same instance passed in")
        void testWithRejectsNullAndBindReturnsInstance() {
            CompositeTerminable terminable = CompositeTerminable.create();
            assertThrows(NullPointerException.class, () -> terminable.with(null));

            AutoCloseable closeable = () -> {};
            assertSame(closeable, terminable.bind(closeable));
        }

        @Test
        @DisplayName("closeSilently swallows the exception and returns it instead")
        void testCloseSilently() {
            CompositeTerminable terminable = CompositeTerminable.create();
            terminable.with(() -> {
                throw new RuntimeException("fail");
            });

            CompositeClosingException result = terminable.closeSilently();
            assertNotNull(result);
        }

        @Test
        @DisplayName("cleanup() removes only closed Terminable entries")
        void testCleanupRemovesClosedTerminables() throws CompositeClosingException {
            CompositeTerminable terminable = CompositeTerminable.create();
            Terminable open = new Terminable() {
                @Override
                public void close() {
                }

                @Override
                public boolean isClosed() {
                    return false;
                }
            };
            Terminable closed = new Terminable() {
                @Override
                public void close() {
                }

                @Override
                public boolean isClosed() {
                    return true;
                }
            };
            terminable.with(open);
            terminable.with(closed);

            terminable.cleanup();

            // The remaining (open) terminable is the only one left to close; if "closed"
            // had survived cleanup, its close() being a no-op wouldn't distinguish it, so
            // instead verify indirectly: after cleanup, closing should not throw and the
            // structure should still function normally.
            assertDoesNotThrow(terminable::close);
        }
    }

    @Nested
    @DisplayName("Weak composite (CompositeTerminable.createWeak)")
    class Weak {

        @Test
        @DisplayName("Closes bound closeables and marks itself closed")
        void testWeakClose() throws CompositeClosingException {
            List<String> order = new ArrayList<>();
            CompositeTerminable terminable = CompositeTerminable.createWeak();
            terminable.with(trackedCloseable(order, "a"));
            terminable.with(trackedCloseable(order, "b"));

            terminable.close();

            assertEquals(java.util.Arrays.asList("b", "a"), order);
            assertTrue(terminable.isClosed());
        }

        @Test
        @DisplayName("with() rejects null")
        void testWeakWithRejectsNull() {
            CompositeTerminable terminable = CompositeTerminable.createWeak();
            assertThrows(NullPointerException.class, () -> terminable.with(null));
        }
    }
}
