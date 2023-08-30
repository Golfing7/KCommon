package com.golfing8.kcommon.command.exc;

/**
 * Occurs when building a command would make the command illegal.
 */
public class CommandInstantiationException extends RuntimeException {
    public CommandInstantiationException(String message) {
        super(message);
    }
}
