package com.golfing8.kcommon.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interface to allow for easier command initialization.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Cmd {
    String GENERATE_PERMISSION = "@generate";

    /**
     * The name of the command.
     */
    String name();

    /**
     * The aliases of the command.
     */
    String[] aliases() default {};

    /**
     * The permission to use for this command.
     * <p>
     * If set to the sentinel {@link #GENERATE_PERMISSION}, this will generate a permission with the following format:
     * <br>
     * <code>PLUGIN_NAME.command[.{MODULE_NAME}].{COMMAND_NAME}[.{SUB_COMMAND}...]</code>
     * <br>
     * As an example, the command <code>/kmodules enable (some-module)</code> would have the permission 'kcommon.command.kmodules.enable'.
     * </p>
     */
    String permission() default GENERATE_PERMISSION;

    /**
     * The visibility of this command.
     */
    CommandVisibility visibility() default CommandVisibility.PROTECTED;

    String description() default "No provided description";

    /**
     * If the command should be only for players.
     */
    boolean forPlayers() default false;
}
