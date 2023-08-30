package com.golfing8.kcommon.config.commented;

/**
 * Thrown when a value cannot be set in a config.
 */
public class UnrecognizedConfigValueException extends IllegalArgumentException {
    public UnrecognizedConfigValueException(Class<?> clazz, String path) {
        super(String.format("Cannot serialize class %s at path %s!", clazz.getSimpleName(), path));
    }
}
