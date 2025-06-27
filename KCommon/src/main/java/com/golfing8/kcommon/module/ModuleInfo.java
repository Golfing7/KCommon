package com.golfing8.kcommon.module;

import com.golfing8.kcommon.config.generator.ConfigClassSource;
import com.golfing8.kcommon.config.lang.LangConfigEnum;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    String name();

    /**
     * The modules which this module depends on. Using this will enforce that, when reflectively loaded, this module
     * loads after the others.
     *
     * @return the dependencies of this module.
     */
    @NotNull
    String[] moduleDependencies() default {};

    /**
     * A list of dependencies that this module depends on. If a module lacks one of these plugins, it will not
     * be registered as a module.
     *
     * @return the module's plugin dependencies.
     */
    @NotNull
    String[] pluginDependencies() default {};

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

    /**
     * Config sources to load
     *
     * @return the config sources
     */
    @NotNull
    Class<? extends ConfigClassSource>[] configSources() default {};

    /**
     * The lang config sources to load
     *
     * @return the lang sources
     */
    @NotNull
    Class<? extends LangConfigEnum>[] langSources() default {};
}
