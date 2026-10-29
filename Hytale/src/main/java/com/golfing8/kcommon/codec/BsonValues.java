package com.golfing8.kcommon.codec;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bson.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts the BSON Hytale's codecs read and write to and from the plain values YAML and Gson use.
 */
public final class BsonValues {
    private BsonValues() {
    }

    /**
     * Converts a plain value (maps, lists, strings, numbers, booleans and null) to BSON.
     *
     * @param value the value
     * @return the BSON
     */
    public static BsonValue fromPlain(@Nullable Object value) {
        if (value == null)
            return BsonNull.VALUE;
        if (value instanceof Map) {
            BsonDocument document = new BsonDocument();
            ((Map<?, ?>) value).forEach((key, child) -> document.put(String.valueOf(key), fromPlain(child)));
            return document;
        }
        if (value instanceof List) {
            BsonArray array = new BsonArray();
            ((List<?>) value).forEach(child -> array.add(fromPlain(child)));
            return array;
        }
        if (value instanceof Integer || value instanceof Short || value instanceof Byte)
            return new BsonInt32(((Number) value).intValue());
        if (value instanceof Long)
            return new BsonInt64((Long) value);
        if (value instanceof Number)
            return new BsonDouble(((Number) value).doubleValue());
        if (value instanceof Boolean)
            return BsonBoolean.valueOf((Boolean) value);
        return new BsonString(value.toString());
    }

    /**
     * Converts BSON to plain values, keeping integers apart from decimals.
     *
     * @param value the BSON
     * @return the plain value
     */
    public static @Nullable Object toPlain(BsonValue value) {
        switch (value.getBsonType()) {
            case DOCUMENT: {
                Map<String, Object> map = new LinkedHashMap<>();
                value.asDocument().forEach((key, child) -> map.put(key, toPlain(child)));
                return map;
            }
            case ARRAY: {
                List<Object> list = new ArrayList<>();
                value.asArray().forEach(child -> list.add(toPlain(child)));
                return list;
            }
            case INT32:
                return value.asInt32().getValue();
            case INT64:
                return value.asInt64().getValue();
            case DOUBLE:
                return value.asDouble().getValue();
            case BOOLEAN:
                return value.asBoolean().getValue();
            case STRING:
                return value.asString().getValue();
            case NULL:
            case UNDEFINED:
                return null;
            default:
                throw new IllegalArgumentException("Unsupported BSON type " + value.getBsonType());
        }
    }

    /**
     * Converts a Gson tree to BSON.
     *
     * @param element the element
     * @return the BSON
     */
    public static BsonValue fromJson(JsonElement element) {
        if (element == null || element.isJsonNull())
            return BsonNull.VALUE;
        if (element.isJsonObject()) {
            BsonDocument document = new BsonDocument();
            element.getAsJsonObject().entrySet().forEach(entry -> document.put(entry.getKey(), fromJson(entry.getValue())));
            return document;
        }
        if (element.isJsonArray()) {
            BsonArray array = new BsonArray();
            element.getAsJsonArray().forEach(child -> array.add(fromJson(child)));
            return array;
        }
        JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isBoolean())
            return BsonBoolean.valueOf(primitive.getAsBoolean());
        if (primitive.isString())
            return new BsonString(primitive.getAsString());
        // Gson doesn't remember if a number had a decimal point, so read it from the text.
        String number = primitive.getAsString();
        if (number.contains(".") || number.contains("e") || number.contains("E"))
            return new BsonDouble(primitive.getAsDouble());
        long whole = primitive.getAsLong();
        return whole == (int) whole ? new BsonInt32((int) whole) : new BsonInt64(whole);
    }

    /**
     * Converts BSON to a Gson tree.
     *
     * @param value the BSON
     * @return the element
     */
    public static JsonElement toJson(BsonValue value) {
        switch (value.getBsonType()) {
            case DOCUMENT: {
                JsonObject object = new JsonObject();
                value.asDocument().forEach((key, child) -> object.add(key, toJson(child)));
                return object;
            }
            case ARRAY: {
                JsonArray array = new JsonArray();
                value.asArray().forEach(child -> array.add(toJson(child)));
                return array;
            }
            case INT32:
                return new JsonPrimitive(value.asInt32().getValue());
            case INT64:
                return new JsonPrimitive(value.asInt64().getValue());
            case DOUBLE:
                return new JsonPrimitive(value.asDouble().getValue());
            case BOOLEAN:
                return new JsonPrimitive(value.asBoolean().getValue());
            case STRING:
                return new JsonPrimitive(value.asString().getValue());
            case NULL:
            case UNDEFINED:
                return JsonNull.INSTANCE;
            default:
                throw new IllegalArgumentException("Unsupported BSON type " + value.getBsonType());
        }
    }
}
