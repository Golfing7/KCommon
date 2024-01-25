package com.golfing8.kcommon.config.lang;

import org.bukkit.command.CommandSender;

/**
 * An interface for a language enum to implement.
 * <p>
 * This simply acts as a marker for the enum to load a given message.
 * </p>
 * <p>
 * Enum values should use standard enum naming convention e.g. {@code SECTION$SOME_MESSAGE_NAME}.
 * In which, this will be parsed to {@code section.some-message-name} for yaml.
 * </p>
 */
public interface LangEnum {
    /**
     * Gets the message that this enum instance loaded.
     *
     * @return the message.
     */
    Message getMessage();

    /**
     * Sets the message this lang enum contains.
     *
     * @param message the message.
     */
    void setMessage(Message message);
}
