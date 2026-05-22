package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.struct.reflection.FieldType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CAComponent implements ConfigAdapter<Component> {
    @Override
    public Class<Component> getAdaptType() {
        return Component.class;
    }

    @Override
    public @Nullable Component toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return ComponentUtils.toComponent(entry.getPrimitive().toString());
    }

    @Override
    public ConfigPrimitive toPrimitive(@NonNull Component object) {
        return ConfigPrimitive.ofString(MiniMessage.miniMessage().serialize(object));
    }
}
