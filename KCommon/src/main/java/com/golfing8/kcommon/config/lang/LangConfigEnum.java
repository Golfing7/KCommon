package com.golfing8.kcommon.config.lang;

/**
 * An interface for the language config.
 */
public interface LangConfigEnum extends MessageContainer {
    /**
     * Gets the message associated with this instance
     *
     * @return the message
     */
    Message getMessage();

    /**
     * Sets the message associated with this instance
     *
     * @param message the message
     */
    void setMessage(Message message);
}
