package com.golfing8.kcommon.config.adapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simple marker interface for an object that can be serialized reflectively.
 */
public interface CASerializable {
    /**
     * Called when this object has finished deserialization.
     */
    default void onDeserialize() {}

    /**
     * Called when this object has finished deserialization.
     */
    default void onDeserialize(ConfigPrimitive primitive) {}

    /**
     * Called when this object is being serialized.
     */
    default void onSerialize() {}

    @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
    @interface Options {
        /**
         * If set to true, this will try to flatten the serialization.
         *
         * @return if flattening should happen.
         */
        boolean flatten() default false;
    }
}
