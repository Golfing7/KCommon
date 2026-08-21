package com.golfing8.kcommon.struct.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CachedValueTest {

    /**
     * A simple test double whose validity can be toggled by the test.
     */
    static final class ManualCachedValue<T> implements CachedValue<T> {
        private T value;
        private boolean valid = false;

        @Override
        public T get() {
            return value;
        }

        @Override
        public void set(T value) {
            this.value = value;
            this.valid = true;
        }

        @Override
        public boolean cacheValid() {
            return valid;
        }

        void invalidate() {
            valid = false;
        }
    }

    @Test
    void testUpdateRunsSupplierWhenCacheInvalid() {
        ManualCachedValue<String> cachedValue = new ManualCachedValue<>();

        String result = cachedValue.update(() -> "computed");

        assertEquals("computed", result);
        assertEquals("computed", cachedValue.get());
    }

    @Test
    void testUpdateDoesNotRunSupplierWhenCacheValid() {
        ManualCachedValue<String> cachedValue = new ManualCachedValue<>();
        cachedValue.set("initial");

        String result = cachedValue.update(() -> {
            throw new AssertionError("Supplier should not run when cache is valid");
        });

        assertEquals("initial", result);
    }

    @Test
    void testUpdateRecomputesAfterInvalidation() {
        ManualCachedValue<String> cachedValue = new ManualCachedValue<>();
        cachedValue.set("initial");
        cachedValue.invalidate();

        String result = cachedValue.update(() -> "refreshed");

        assertEquals("refreshed", result);
        assertEquals("refreshed", cachedValue.get());
    }
}
