package com.golfing8.kcommon.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.util.RawJsonReader;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeCodec implements Codec<ZonedDateTime> {
    @Override
    public @Nullable ZonedDateTime decode(BsonValue bsonValue, ExtraInfo extraInfo) {
        BsonDocument document = bsonValue.asDocument();
        String zoneId = document.getString("ZoneId", new BsonString("America/New_York")).getValue();
        int year = document.getInt32("Year").getValue();
        int month = document.getInt32("Month").getValue();
        int day = document.getInt32("Day").getValue();
        int hour = document.getInt32("Hour").getValue();
        int minute = document.getInt32("Minute").getValue();
        int second = document.getInt32("Second").getValue();

        return ZonedDateTime.of(
                year,
                month,
                day,
                hour,
                minute,
                second,
                0,
                ZoneId.of(zoneId)
        );
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nullable ZonedDateTime decodeJson(@NonNull RawJsonReader reader, ExtraInfo extraInfo) throws IOException {
        return decode(RawJsonReader.readBsonDocument(reader), extraInfo);
    }

    @Override
    public BsonValue encode(ZonedDateTime zonedDateTime, ExtraInfo extraInfo) {
        BsonDocument document = new BsonDocument();
        document.put("ZoneId", new BsonString(zonedDateTime.getZone().toString()));
        document.put("Year", new BsonInt32(zonedDateTime.getYear()));
        document.put("Month", new BsonInt32(zonedDateTime.getMonthValue()));
        document.put("Day", new BsonInt32(zonedDateTime.getDayOfMonth()));
        document.put("Hour", new BsonInt32(zonedDateTime.getHour()));
        document.put("Minute", new BsonInt32(zonedDateTime.getMinute()));
        document.put("Second", new BsonInt32(zonedDateTime.getSecond()));
        return document;
    }

    @Override
    public @NonNull Schema toSchema(@NonNull SchemaContext schemaContext) {
        return null;
    }
}
