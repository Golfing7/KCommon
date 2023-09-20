package com.golfing8.kcommon.struct.filter;

/**
 * A filter for a certain object.
 * <p>
 * Provides a single {@link #filter(Object)} method for application.
 * A result of zero should represent this item not being filtered at all.
 * Any other value should represent the level of filtering applied to the object, higher numbers being a more 'important' filtration.
 * </p>
 */
public interface Filter<T> {
    /**
     * Attempts to apply this filter to the given object.
     *
     * @param obj the obj.
     * @return the level of filtration.
     */
    int filter(T obj);
}
