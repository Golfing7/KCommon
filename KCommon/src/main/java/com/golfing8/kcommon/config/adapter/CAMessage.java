package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.config.lang.PagedMessage;
import com.golfing8.kcommon.struct.SoundWrapper;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.title.Title;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Adapts instances of {@link Message}
 */
public class CAMessage implements ConfigAdapter<Message> {
    @Override
    public Class<Message> getAdaptType() {
        return Message.class;
    }

    @Override
    public Message toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return Message.builder().build();

        return new Message(entry.unwrap());
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConfigPrimitive toPrimitive(@NotNull Message object) {
        if (object.isSimple()) {
            if (object.getMessages() == null)
                return ConfigPrimitive.ofList(Lists.newArrayList());

            return object.getMessages().size() == 1 ?
                    ConfigPrimitive.ofString(object.getMessages().get(0)) :
                    ConfigPrimitive.ofList(object.getMessages());
        }
        Map<String, Object> items = new HashMap<>();
        if (object.isPaged()) {
            items.put("paged", true);
        }
        if (object.getPageHeader() != null && !Objects.equals(PagedMessage.DEFAULT_PAGE_HEADER, object.getPageHeader())) {
            items.put("page-header", object.getPageHeader());
        }
        if (object.getPageFooter() != null && !Objects.equals(PagedMessage.DEFAULT_PAGE_FOOTER, object.getPageFooter())) {
            items.put("page-footer", object.getPageFooter());
        }
        if (object.getPageHeight() > 0 && !Objects.equals(PagedMessage.DEFAULT_PAGE_HEIGHT, object.getPageHeight())) {
            items.put("page-height", object.getPageHeight());
        }
        if (object.getMessages() != null) {
            items.put("message", new ArrayList<>(object.getMessages())); // Clone to avoid aliasing.
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
