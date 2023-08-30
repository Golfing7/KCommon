package com.golfing8.kcommon.struct.ptr;

import com.golfing8.kcommon.nms.reflection.FieldHandle;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This class acts as a pointer to a field.
 * The implementation of equals and hashcode follows the value of the tracked field, rather than any of the fields in this class.
 */
@RequiredArgsConstructor
public class FieldPointer<T> {
    /** The instance of the object */
    private final @Nullable Object instance;
    /** The handle of the field */
    private final FieldHandle<T> fieldHandle;

    /**
     * Gets the object's value.
     *
     * @return the object's value.
     */
    public T get() {
        return fieldHandle.get(instance);
    }

    /**
     * Sets the value of the field.
     *
     * @param value the value.
     */
    public void set(T value) {
        fieldHandle.set(instance, value);
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
