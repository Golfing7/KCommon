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

    /**
     * Builds {@link CodecSettings}.
     *
     * @param <T> the type
     */
    public static class Builder<T> {
        private BuilderCodec<? super T> parent;
        private BiConsumer<T, ExtraInfo> callback;

        /**
         * Sets the parent codec, whose fields are inherited.
         *
         * @param parent the parent codec
         * @return this builder
         */
        public Builder<T> parent(BuilderCodec<? super T> parent) {
            this.parent = parent;
            return this;
        }

        /**
         * Sets the callback run on each object after it is decoded.
         *
         * @param c the callback
         * @return this builder
         */
        public Builder<T> callback(Consumer<T> c) {
            this.callback = (obj, _) -> c.accept(obj);
            return this;
        }

        /**
         * Sets the callback run on each object after it is decoded.
         *
         * @param c the callback, also given the decoding info
         * @return this builder
         */
        public Builder<T> callback(BiConsumer<T, ExtraInfo> c) {
            this.callback = c;
            return this;
        }

        /**
         * Builds the settings.
         *
         * @return the settings
         */
        public CodecSettings<T> build() {
            return new CodecSettings<>(parent, callback);
        }
    }
}
