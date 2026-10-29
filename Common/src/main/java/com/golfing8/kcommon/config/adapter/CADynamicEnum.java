package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.DynamicEnum;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Adapts instances of {@link DynamicEnum}
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CADynamicEnum implements ConfigAdapter<DynamicEnum> {
    @Override
    public Class<DynamicEnum> getAdaptType() {
        return DynamicEnum.class;
    }

    @Override
    public @Nullable DynamicEnum toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Optional<? extends DynamicEnum<?>> optional = DynamicEnum.valueOf((Class<? extends DynamicEnum>) type.getType(), entry.getPrimitive().toString());
        return optional.orElse(null);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull DynamicEnum object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
