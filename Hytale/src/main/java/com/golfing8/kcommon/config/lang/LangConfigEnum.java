package com.golfing8.kcommon.config.lang;

/**
 * An enum of language constants, loaded into a module's lang file.
 * Name the constants like the keys: {@code SOME_MESSAGE} becomes {@code some-message}.
 */
public interface LangConfigEnum extends MessageContainer {
    @Override
    Message getMessage();

    /**
     * Sets the message after it was loaded from the lang file.
     *
     * @param message the message.
     */
    void setMessage(Message message);
}
