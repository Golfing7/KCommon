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
         * <p>
         * Assume you have a type that only has a single serializable field that is an int.
         * In this case, instead of serializing the type like:
         * </p>
         * <pre>
         * {@code
         * some-parent-section:
         *     serialized-field: 5
         * }
         * it will be serialized as follows:
         * {@code
         * some-parent-section: 5
         * }
         * </pre>
         *
         * @return if flattening should happen.
         */
        boolean flatten() default false;

        /**
         * If set to true, this will allow a user to specify a path string rather than a section where the data is loaded from.
         * This can help to reduce config redundancy without forcing the developer to write a special
         * config adapter that uses a registry.
         *
         * @return if the type can delegate.
         */
        boolean canDelegate() default false;

        /**
         * The class to serialize 'up to'. (Parent private fields will be deserialized until and including reaching this parent class)
         * If this value uses to Object class, only the base class will be deserialized.
         * @return the class to serialize up to.
         */
        Class<?> serializeUpTo() default Object.class;
    }
}
