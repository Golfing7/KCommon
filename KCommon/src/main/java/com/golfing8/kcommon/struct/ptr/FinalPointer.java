package com.golfing8.kcommon.struct.ptr;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * An implementation of the {@link Pointer} interface with a final value.
 * @param <T>
 */
@RequiredArgsConstructor
public class FinalPointer<T> {
    /** The value of this pointer */
    private final T value;

    /**
     * Gets the object's value.
     *
     * @return the object's value.
     */
    public T get() {
        return value;
    }

    /**
     * Sets the value of the field.
     *
     * @param value the value.
     */
    public void set(T value) {
        throw new UnsupportedOperationException("Cannot set value on final pointer");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object obj) {
        if (!(obj instanceof Pointer))
            return false;

        Pointer pointer = (Pointer) obj;
        return Objects.equals(get(), pointer.get());
    }

    @Override
    public int hashCode() {
        return Objects.hash(get());
    }
}
