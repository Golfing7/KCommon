package com.golfing8.kcommon.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interface to allow for easier command initialization.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Cmd {
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
     */
    String permission() default "";

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
