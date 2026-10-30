package com.golfing8.kcommon.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

/**
 * Stores a UUID as its string form.
 */
public class UUIDStringCodec implements Codec<UUID> {
    @Override
    public @Nullable UUID decode(BsonValue bsonValue, ExtraInfo extraInfo) {
        return bsonValue.isNull() ? null : UUID.fromString(bsonValue.asString().getValue());
    }

    @Override
    public @Nullable UUID decodeJson(@NonNull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        return UUID.fromString(reader.readString());
    }

    @Override
    public BsonValue encode(UUID uuid, ExtraInfo extraInfo) {
        return new BsonString(uuid.toString());
    }

    @Override
    public @NonNull Schema toSchema(@NonNull SchemaContext schemaContext) {
        Schema schema = new Schema();
        schema.setTitle("UUID");
        return schema;
    }
}
