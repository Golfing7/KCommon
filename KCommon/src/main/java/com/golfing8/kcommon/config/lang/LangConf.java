package com.golfing8.kcommon.config.lang;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker for fields on classes extending {@link LangConfigContainer}. This marker allows for messages with this annotation
 * to be reflectively loaded.
 * <p>
 * Fields using this annotation should be non-null.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface LangConf {
    String path() default "";
}
