package com.golfing8.kcommon.config.adapter;

/**
 * A config adapter implementation for enums.
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
    public ConfigPrimitive toPrimitive(Enum object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
