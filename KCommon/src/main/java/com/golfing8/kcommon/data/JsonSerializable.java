package com.golfing8.kcommon.data;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

/**
 * Represents something that can be serialized to and from JSON.
 * <p />
 * Classes that implement this interface should have a no args constructor for deserialization.
 */
public interface JsonSerializable {
    /**
     * Serializes this object as a JsonObject to be stored in a {@link DataManager}
     *
     * @return the json object representative of this object, or null if it should not be serialized.
     */
    @Nullable JsonObject serialize();

    /**
     * Deserializes the data found in the JsonObject to this instance.
     *
     * @param object the json object to deserialize from
     */
    void deserialize(JsonObject object);
}
