package com.golfing8.kcommon.data.serializer;

import com.golfing8.kcommon.codec.BsonValues;
import com.golfing8.kcommon.codec.HytaleCodecs;
import com.golfing8.kcommon.data.DataSerializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;

import java.io.IOException;

/**
 * Lets data managers store any type with a Hytale codec (see {@link HytaleCodecs}) the way the codec writes it.
 */
public class HytaleDataSerializerExtension implements DataSerializerExtension {
    @Override
    public void configure(GsonBuilder builder) {
        builder.registerTypeAdapterFactory(new CodecAdapterFactory());
    }

    static final class CodecAdapterFactory implements TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            Class<? super T> raw = typeToken.getRawType();
            // Stored objects keep Gson's field serialization; java.lang types already have Gson adapters.
            if (DataSerializable.class.isAssignableFrom(raw) || raw.isPrimitive() || raw.getName().startsWith("java.lang."))
                return null;

            @SuppressWarnings("unchecked")
            Codec<T> codec = (Codec<T>) HytaleCodecs.find(raw);
            if (codec == null)
                return null;

            TypeAdapter<JsonElement> elements = gson.getAdapter(JsonElement.class);
            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    elements.write(out, BsonValues.toJson(codec.encode(value, new ExtraInfo())));
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    return codec.decode(BsonValues.fromJson(elements.read(in)), new ExtraInfo());
                }
            }.nullSafe();
        }
    }
}
