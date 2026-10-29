package com.golfing8.kcommon.config.generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Acts as a method of a field giving extra data about how it would like to be serialized.
 * Primarily used for comments.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Conf {
    String DEFAULT_CONF = "@default";

    /**
     * The comment lines to add to a configuration value.
     *
     * @return the comment.
     */
    String[] value() default {};

    /**
     * The label for the value in the config
     *
     * @return the label
     */
    String label() default "";

    /**
     * The config to map to. This is only used in Module contexts
     *
     * @return the config
     */
    String config() default DEFAULT_CONF;
}
