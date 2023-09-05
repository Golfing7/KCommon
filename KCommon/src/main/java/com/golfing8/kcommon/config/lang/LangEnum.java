package com.golfing8.kcommon.config.lang;

/**
 * An interface for a language enum to implement.
 * <p>
 * This simply acts as a marker for the enum to load a given message.
 * </p>
 */
public interface LangEnum {
    /**
     * Gets the message that this enum instance loaded.
     *
     * @return the message.
     */
    Object getMessage();
}
