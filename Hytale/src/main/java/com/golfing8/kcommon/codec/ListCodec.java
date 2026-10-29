package com.golfing8.kcommon.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.WrappedCodec;
import com.hypixel.hytale.codec.exception.CodecException;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.ArraySchema;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import org.bson.BsonArray;
import org.bson.BsonValue;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * A codec for lists based on the Hytale {@link com.hypixel.hytale.codec.codecs.set.SetCodec}
 */
public class ListCodec<V, S extends List<V>> implements Codec<List<V>>, WrappedCodec<V> {
    private final Codec<V> codec;
    private final Supplier<S> supplier;
    private final boolean unmodifiable;

    public ListCodec(Codec<V> codec, Supplier<S> supplier, boolean unmodifiable) {
        this.codec = codec;
        this.supplier = supplier;
        this.unmodifiable = unmodifiable;
    }

    public List<V> decode(@Nonnull BsonValue bsonValue, @Nonnull ExtraInfo extraInfo) {
        BsonArray list = bsonValue.asArray();
        if (list.isEmpty()) {
            return this.unmodifiable ? Collections.emptyList() : this.supplier.get();
        } else {
            S out = this.supplier.get();

            for (int i = 0; i < list.size(); ++i) {
                BsonValue value = list.get(i);
                extraInfo.pushIntKey(i);

                try {
                    V decoded = (V) this.codec.decode(value, extraInfo);
                    out.add(decoded);
                } catch (Exception e) {
                    throw new CodecException("Failed to decode", value, extraInfo, e);
                } finally {
                    extraInfo.popKey();
                }
            }

            if (this.unmodifiable) {
                return Collections.unmodifiableList(out);
            } else {
                return out;
            }
        }
    }

    public List<V> decodeJson(@Nonnull RawJsonReader reader, @Nonnull ExtraInfo extraInfo) throws IOException {
        reader.expect('[');
        reader.consumeWhiteSpace();
        if (reader.tryConsume(']')) {
            return this.unmodifiable ? Collections.emptyList() : this.supplier.get();
        } else {
            int i = 0;
            S out = this.supplier.get();

            while (true) {
                extraInfo.pushIntKey(i, reader);

                try {
                    V decoded = (V) this.codec.decodeJson(reader, extraInfo);
                    out.add(decoded);
                    ++i;
                } catch (Exception e) {
                    throw new CodecException("Failed to decode", reader, extraInfo, e);
                } finally {
                    extraInfo.popKey();
                }

                reader.consumeWhiteSpace();
                if (reader.tryConsumeOrExpect(']', ',')) {
                    return this.unmodifiable ? Collections.unmodifiableList(out) : out;
                }

                reader.consumeWhiteSpace();
            }
        }
    }

    @Nonnull
    public BsonValue encode(@Nonnull List<V> vs, @Nonnull ExtraInfo extraInfo) {
        BsonArray out = new BsonArray();
        int key = 0;

        for (V v : vs) {
            extraInfo.pushIntKey(key++);

            try {
                out.add(this.codec.encode(v, extraInfo));
            } finally {
                extraInfo.popKey();
            }
        }

        return out;
    }

    @Nonnull
    public Schema toSchema(@Nonnull SchemaContext context) {
        ArraySchema schema = new ArraySchema();
        schema.setTitle("List");
        schema.setItem(context.refDefinition(this.codec));
        schema.setUniqueItems(false);
        return schema;
    }

    public Codec<V> getChildCodec() {
        return this.codec;
    }
}
