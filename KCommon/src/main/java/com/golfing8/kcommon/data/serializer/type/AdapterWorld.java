package com.golfing8.kcommon.data.serializer.type;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

/**
 * A type adapter for java locations.
 *
 * Adaptations are simply done in World -> String & String -> World form.
 */
public enum AdapterWorld implements JsonSerializer<World>, JsonDeserializer<World> {
    INSTANCE;

    @Override
    public World deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Bukkit.getWorld(jsonElement.getAsString());
    }

    @Override
    public JsonElement serialize(World world, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(world.getName());
    }
}
