package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.codec.BsonValues;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Stores a Hytale type in config in the same form its {@link Codec} reads and writes.
 *
 * @param <T> the type
 */
public class CAHytaleCodec<T> implements ConfigAdapter<T> {
    private final Class<T> type;
    private final Codec<T> codec;

    public CAHytaleCodec(Class<T> type, Codec<T> codec) {
        this.type = type;
        this.codec = codec;
    }

    @Override
    public Class<T> getAdaptType() {
        return type;
    }

    @Override
    public @Nullable T toPOJO(ConfigPrimitive entry, FieldType fieldType) {
        Object value = entry.unwrap();
        return value == null ? null : codec.decode(BsonValues.fromPlain(value), new ExtraInfo());
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull T object) {
        return ConfigPrimitive.ofNullable(BsonValues.toPlain(codec.encode(object, new ExtraInfo())));
    }
}
