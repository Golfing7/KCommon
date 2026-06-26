package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.nms.struct.BookData;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.MS;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A config adapter for book data
 */
public class CABookData implements ConfigAdapter<BookData> {
    @Override
    public Class<BookData> getAdaptType() {
        return BookData.class;
    }

    @Override
    public @Nullable BookData toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> data = entry.unwrap();
        BookData.BookDataBuilder builder = BookData.builder();
        if (data.containsKey("author")) {
            builder.author(MS.toComponent(data.get("author").toString()));
        }
        if (data.containsKey("title")) {
            builder.title(MS.toComponent(data.get("title").toString()));
        }
        if (data.containsKey("pages")) {
            List<Component> pages = ConfigTypeRegistry.getFromType(entry.getSubValue("pages"), FieldType.extractFrom(new TypeToken<List<Component>>() {}));
            builder.pages(pages);
        }

        return builder.build();
    }

    @Override
    public ConfigPrimitive toPrimitive(@NonNull BookData object) {
        Map<String, Object> data = new HashMap<>();
        if (object.getAuthor() != null) {
            data.put("author", MiniMessage.miniMessage().serialize(object.getAuthor()));
        }
        if (object.getTitle() != null) {
            data.put("title", MiniMessage.miniMessage().serialize(object.getTitle()));
        }
        if (object.getPages() != null) {
            data.put("pages", object.getPages().stream().map(MiniMessage.miniMessage()::serialize).collect(Collectors.toList()));
        }
        return ConfigPrimitive.ofMap(data);
    }
}
