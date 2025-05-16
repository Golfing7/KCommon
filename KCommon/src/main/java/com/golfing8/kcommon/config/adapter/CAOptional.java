package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Optional;

@SuppressWarnings({"rawtypes"})
public class CAOptional implements ConfigAdapter<Optional> {
    @Override
    public Class<Optional> getAdaptType() {
        return Optional.class;
    }

    @Override
    public @Nullable Optional toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return Optional.empty();

        Type actualType = type.getGenericTypes().get(0);
        ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(type.getGenericTypes().get(0));
        if (adapter == null)
            return Optional.of(entry.unwrap());

        return Optional.ofNullable(adapter.toPOJO(entry, new FieldType(actualType)));
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Optional object) {
        if (object.isPresent()) {
            return ConfigTypeRegistry.toPrimitive(object.get());
        }
        return ConfigPrimitive.ofNull();
    }
}
