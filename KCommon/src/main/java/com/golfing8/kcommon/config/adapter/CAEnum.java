package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * Adapts instances of {@link Enum}
 * <p>
 * In particular, any type of enum is supported.
 * </p>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CAEnum implements ConfigAdapter<Enum> {
    @Override
    public Class<Enum> getAdaptType() {
        return Enum.class;
    }

    @Override
    public Enum toPOJO(ConfigPrimitive entry, FieldType fieldType) {
        if (entry.getPrimitive() == null)
            return null;

        Object primitive = entry.getPrimitive();
        Class<? extends Enum> actualType = (Class<? extends Enum>) fieldType.getType();
        for (Enum en : actualType.getEnumConstants()) {
            if (en.name().equals(primitive.toString()))
                return en;
        }
        return null;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Enum object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
