package com.golfing8.kcommon.config.lang;

/**
 * An interface for the language config.
 */
public interface LangConfigEnum extends MessageContainer {
    Message getMessage();

    void setMessage(Message message);
}
