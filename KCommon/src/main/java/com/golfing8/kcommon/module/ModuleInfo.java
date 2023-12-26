package com.golfing8.kcommon.module;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A module's information, used when reflectively setting up features.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleInfo {
    /**
     * The name of the module.
     * @return the name of the module.
     */
    String name();

    /**
     * The modules which this module depends on. Using this will enforce that, when reflectively loaded, this module
     * loads after the others.
     *
     * @return the dependencies of this module.
     */
    String[] moduleDependencies() default {};

    /**
     * A list of dependencies that this module depends on. If a module lacks one of these plugins, it will not
     * be registered as a module.
     *
     * @return the module's plugin dependencies.
     */
    String[] pluginDependencies() default {};
}
