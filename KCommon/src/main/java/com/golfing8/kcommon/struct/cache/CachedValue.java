package com.golfing8.kcommon.struct.cache;

import java.util.function.Supplier;

/**
 * Represents some value that can be cached.
 */
public interface CachedValue<T> {
    /**
     * Gets whatever value is currently stored in the cache, or null if !{@link #cacheValid()}.
     *
     * @return the value, or null.
     */
    T get();

    /**
     * Sets the cache value.
     *
     * @param value the value of the cache.
     */
    void set(T value);

    /**
     * Checks if the value is still valid.
     *
     * @return if the value is valid.
     */
    boolean cacheValid();

    /**
     * Tries to update the cached value if the current value is invalid.
     * If the cache is not invalid, the original value is returned and the supplier is not run.
     *
     * @param newValue the new value.
     * @return the updated value.
     */
    default T update(Supplier<T> newValue) {
        if (cacheValid())
            return get();

        T value = newValue.get();
        set(value);
        return value;
    }
}
