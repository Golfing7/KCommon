package com.golfing8.kcommon.codec;

import com.golfing8.kcommon.ComponentUtils;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bson.BsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * Stores a text component as a MiniMessage string.
 */
public class TextComponentCodec implements Codec<TextComponent> {
    @Override
    public BsonValue encode(TextComponent component, ExtraInfo extraInfo) {
        return STRING.encode(MiniMessage.miniMessage().serialize(component), extraInfo);
    }

    @Override
    public @Nullable TextComponent decode(BsonValue bsonValue, ExtraInfo extraInfo) {
        return (TextComponent) ComponentUtils.toComponent(STRING.decode(bsonValue, extraInfo));
    }

    @Override
    public @Nullable TextComponent decodeJson(@NonNull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        return (TextComponent) ComponentUtils.toComponent(STRING.decodeJson(reader, extraInfo));
    }

    @Override
    public @NonNull Schema toSchema(@NonNull SchemaContext schemaContext) {
        StringSchema schema = new StringSchema();
        schema.setTitle("TextComponent");
        return schema;
    }
}
