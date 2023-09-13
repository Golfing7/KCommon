package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;

import java.util.LinkedHashMap;

/**
 * An adapter for POJO <-> {@link ConfigPrimitive} values.
 * <p>
 * Each adapter represents a one-to-many model with POJOs.
 * For example, a {@link ConfigAdapter} may implement an adapter for ALL enum types, using {@link String} as the basis.
 * </p>
 * <p>
 * Another note is that when using maps as a primitive, it is recommended to use {@link LinkedHashMap} to maintain key order.
 * </p>
 */
public interface ConfigAdapter<T> {
    /**
     * Gets the type this adapter works on.
     *
     * @return the type.
     */
    Class<T> getAdaptType();

    /**
     * Converts the primitive to a POJO.
     *
     * @param entry the entry.
     * @param type the actual type of POJO we desire.
     * @return the POJO.
     */
    T toPOJO(ConfigPrimitive entry, FieldType type);

    /**
     * Converts the POJO to a config primitive.
     *
     * @param object the POJO.
     * @return the primitive.
     */
    ConfigPrimitive toPrimitive(T object);
}
