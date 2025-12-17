package com.golfing8.kcommon.config.exc;

/**
 * Thrown when a config adapter does not accept a given instance
 */
public class InvalidConfigException extends RuntimeException {
    public InvalidConfigException(String msg) {
        super(msg);
    }
}
