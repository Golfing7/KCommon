package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.config.KSection;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.util.HytaleMessages;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import lombok.Builder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A message from a lang file, sent to Hytale players and the console.
 * <p>
 * Lines use the same formatting as on Bukkit: '&' color codes, hex colors and MiniMessage tags.
 * In config a message is a single string, a list of lines, or a section with a {@code message} key.
 * Bukkit's titles, action bars and sounds aren't supported here yet; those keys are ignored.
 * </p>
 */
@Getter
@Builder
public class Message implements MessageContainer {
    private final @Nullable List<String> messages;

    public Message(@Nullable List<String> messages) {
        this.messages = messages;
    }

    public Message(String message) {
        this(Collections.singletonList(message));
    }

    /**
     * Reads a message from a config value.
     *
     * @param value the value.
     */
    @SuppressWarnings("unchecked")
    public Message(@Nullable Object value) {
        KSection section = ConfigPrimitive.asSection(value);
        if (section != null) {
            value = section.get("message");
        } else if (value instanceof Map) {
            value = ((Map<String, Object>) value).get("message");
        }

        if (value == null) {
            this.messages = null;
        } else if (value instanceof List) {
            List<String> lines = new ArrayList<>();
            for (Object line : (List<Object>) value) {
                lines.add(String.valueOf(line));
            }
            this.messages = lines;
        } else {
            this.messages = Collections.singletonList(value.toString());
        }
    }

    @Override
    public Message getMessage() {
        return this;
    }

    /**
     * Sends this message to the receiver.
     *
     * @param receiver     the receiver, such as a player or the console.
     * @param placeholders the placeholders.
     */
    @Override
    public void send(IMessageReceiver receiver, Object... placeholders) {
        if (messages == null || messages.isEmpty())
            return;

        receiver.sendMessage(HytaleMessages.toMessage(messages, placeholders));
    }
}
