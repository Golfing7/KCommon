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
    default void onDeserialize() {
    }

    /**
     * Called when this object has finished deserialization.
     */
    default void onDeserialize(ConfigPrimitive primitive) {
    }

    /**
     * Called when this object is being serialized.
     */
    default void onSerialize() {
    }

    /**
     * A resolver for finding the right type to deserialize.
     */
    interface TypeResolver {
        /**
         * The type of class to deserialize.
         *
         * @return the type.
         */
        Class<? extends CASerializable> getType();
    }

    /**
     * An annotation for if the field or all fields in a class should be required in the config.
     * If present, any new field (or class) this is applied to will be populated into the config if it is missing.
     * Typically, this should be used for configs as their options need to be visible to the user.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface Required {
        /**
         * If the field is required.
         *
         * @return true/false
         */
        boolean value() default true;
    }

    /**
     * An annotation for defining options on classes implementing {@link CASerializable}
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
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
         * If set to true, this class will be treated as a config class and all fields will be populated in the config.
         * Normally, new fields added
         *
         * @return true if config mode.
         */
        boolean config() default false;

        /**
         * The type resolver enum finds the correct type to deserialize from a special {@code type: ENUM} key.
         *
         * @return the type resolver enum.
         */
        Class<? extends TypeResolver> typeResolverEnum() default TypeResolver.class;
    }
}
