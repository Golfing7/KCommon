package com.golfing8.kcommon.config.adapter;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a config adapter that can only deserialize primitives.
 *
 * @param <T> the type.
 */
public interface ReadOnlyConfigAdapter<T> extends ConfigAdapter<T> {

    @Override
    default ConfigPrimitive toPrimitive(@NotNull T object) {
        throw new UnsupportedOperationException("Read only config adapters can only read");
    }
}
