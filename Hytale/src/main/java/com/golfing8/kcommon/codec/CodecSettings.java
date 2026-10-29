package com.golfing8.kcommon.codec;

import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Settings for codec serialization
 */
@Getter
@AllArgsConstructor
public final class CodecSettings<T> {
    /**
     * The parent's codec
     */
    private final BuilderCodec<? super T> parent;
    /**
     * The callback for after decoding
     */
    private final BiConsumer<T, ExtraInfo> callback;

    /**
     * Generates a new builder for codec settings
     *
     * @param token the token class type
     * @param <T>   the type
     * @return the builder
     */
    public static <T> Builder<T> builder(Class<T> token) {
        return new Builder<>();
    }

    public static class Builder<T> {
        private BuilderCodec<? super T> parent;
        private BiConsumer<T, ExtraInfo> callback;

        public Builder<T> parent(BuilderCodec<? super T> parent) {
            this.parent = parent;
            return this;
        }

        public Builder<T> callback(Consumer<T> c) {
            this.callback = (obj, _) -> c.accept(obj);
            return this;
        }

        public Builder<T> callback(BiConsumer<T, ExtraInfo> c) {
            this.callback = c;
            return this;
        }

        public CodecSettings<T> build() {
            return new CodecSettings<>(parent, callback);
        }
    }
}
