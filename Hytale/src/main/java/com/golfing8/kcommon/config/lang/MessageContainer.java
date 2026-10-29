package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.util.HytaleMessages;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;

/**
 * Something that holds a {@link Message} and can send it.
 */
public interface MessageContainer {
    /**
     * Gets the message.
     *
     * @return the message.
     */
    Message getMessage();

    /**
     * Sends the message to the receiver.
     *
     * @param receiver     the receiver, such as a player or the console.
     * @param placeholders the placeholders.
     */
    default void send(IMessageReceiver receiver, Object... placeholders) {
        getMessage().send(receiver, placeholders);
    }

    /**
     * Sends the message to every online player.
     *
     * @param placeholders the placeholders.
     */
    default void broadcast(Object... placeholders) {
        if (getMessage().getMessages() == null || getMessage().getMessages().isEmpty())
            return;

        com.hypixel.hytale.server.core.Message message = HytaleMessages.toMessage(getMessage().getMessages(), placeholders);
        for (PlayerRef player : Universe.get().getPlayers()) {
            player.sendMessage(message);
        }
    }
}
