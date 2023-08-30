package com.golfing8.kcommon.struct.ptr;

/**
 * This class acts as a pointer to a field.
 * The implementation of equals and hashcode should follow the value of the tracked value.
 */
public interface Pointer<T> {

    /**
     * Gets the object's value.
     *
     * @return the object's value.
     */
    T get();

    /**
     * Sets the value of the field.
     *
     * @param value the value.
     */
    void set(T value);
}
