package com.golfing8.kcommon.versioned;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a group of NMS server versions.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerVersionGroup {
    /**
     * The minimum major version that this module will load on.
     *
     * @return the minimum major version
     */
    int minimumMajorVersion() default -1;

    /**
     * The maximum major version that this module will load on.
     *
     * @return the maximum major version.
     */
    int maximumMajorVersion() default -1;

    /**
     * The minimum minor version that this module will load on.
     *
     * @return the minimum minor version.
     */
    int minimumMinorVersion() default -1;

    /**
     * The maximum minor version that this module will load on.
     *
     * @return the maximum minor version.
     */
    int maximumMinorVersion() default -1;
}
