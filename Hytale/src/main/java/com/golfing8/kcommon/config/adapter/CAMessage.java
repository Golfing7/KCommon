package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Reads and writes Hytale {@link Message}s.
 */
public class CAMessage implements ConfigAdapter<Message> {
    @Override
    public Class<Message> getAdaptType() {
        return Message.class;
    }

    @Override
    public Message toPOJO(ConfigPrimitive entry, FieldType type) {
        Object value = entry.getPrimitive() == null ? null : entry.unwrap();
        return new Message(value);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Message object) {
        if (object.getMessages() == null)
            return ConfigPrimitive.ofList(new ArrayList<>());

        return object.getMessages().size() == 1 ?
                ConfigPrimitive.ofString(object.getMessages().get(0)) :
                ConfigPrimitive.ofList(new ArrayList<>(object.getMessages()));
    }
}
