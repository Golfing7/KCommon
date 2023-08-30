package com.golfing8.kcommon.command;

/**
 * Represents the visibility of a command. This is used when displaying a command's help message for a user.
 */
public enum CommandVisibility {
    // Shown to no one. The command should never be mentioned, ever.
    PRIVATE,
    // Private to users with the permission to execute the command.
    PROTECTED,
    // Public, everyone can see it.
    PUBLIC,
    ;
}
