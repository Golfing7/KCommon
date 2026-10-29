package com.golfing8.kcommon.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public class BetterItemCodec implements Codec<ItemStack> {
    @Override
    public @Nullable ItemStack decode(BsonValue bsonValue, ExtraInfo extraInfo) {
        if (bsonValue.isNull()) {
            return null;
        }

        if (bsonValue.isString()) {
            if (ItemStack.EMPTY.getItemId().equals(bsonValue.asString().getValue())) {
                return ItemStack.EMPTY;
            } else {
                return new ItemStack(bsonValue.asString().getValue());
            }
        }

        if (bsonValue.isDocument()) {
            return ItemStack.CODEC.decode(bsonValue, extraInfo);
        }
        throw new UnsupportedOperationException("Unknown bson for item " + bsonValue);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nullable ItemStack decodeJson(@NonNull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        BsonValue bsonValue = RawJsonReader.readBsonValue(reader);
        return this.decode(bsonValue, extraInfo);
    }

    @Override
    public BsonValue encode(ItemStack itemStack, ExtraInfo extraInfo) {
        if (itemStack == null) {
            return new BsonNull();
        } else if (itemStack.isEmpty() || isBasic(itemStack)) {
            return new BsonString(itemStack.getItemId());
        } else {
            return ItemStack.CODEC.encode(itemStack, extraInfo);
        }
    }

    @Override
    public @NonNull Schema toSchema(@NonNull SchemaContext schemaContext) {
        return ItemStack.CODEC.toSchema(schemaContext);
    }

    private static boolean isBasic(ItemStack itemStack) {
        return new ItemStack(itemStack.getItemId()).equals(itemStack);
    }
}
