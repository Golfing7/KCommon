package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.struct.SoundWrapper;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.title.Title;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CAMessage implements ConfigAdapter<Message> {
    @Override
    public Class<Message> getAdaptType() {
        return Message.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Message toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getSource() != null && entry.getPrimitive() != null)
            return new Message(entry.getSource());

        return new Message((List<String>) entry.getPrimitive(), null, null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigPrimitive toPrimitive(Message object) {
        if (object.isSimple()) {
            return object.getMessages().size() == 1 ?
                    ConfigPrimitive.ofString(object.getMessages().get(0)) :
                    ConfigPrimitive.ofList(object.getMessages());
        }
        Map<String, Object> items = new HashMap<>();
        if (object.getMessages() != null) {
            items.put("message", object.getMessages());
        }
        if (object.getSounds() != null) {
            Map<String, Object> sounds = new HashMap<>();
            int count = 0;
            ConfigAdapter<SoundWrapper> adapter = (ConfigAdapter<SoundWrapper>) ConfigTypeRegistry.findAdapter(SoundWrapper.class);
            if (adapter == null)
                throw new IllegalStateException("Missing sound wrapper config adapter");

            for (SoundWrapper wrapper : object.getSounds()) {
                sounds.put(String.valueOf(count++), adapter.toPrimitive(wrapper).getPrimitive());
            }
            items.put("sounds", sounds);
        }
        if (object.getActionBar() != null) {
            items.put("actionbar", object.getActionBar());
        }
        if (object.getTitle() != null) {
            ConfigAdapter<Title> adapter = (ConfigAdapter<Title>) ConfigTypeRegistry.findAdapter(Title.class);
            if (adapter == null)
                throw new IllegalStateException("Missing title config adapter");

            items.put("title", adapter.toPrimitive(object.getTitle()).getPrimitive());
        }
        return ConfigPrimitive.ofMap(items);
    }
}
