package com.golfing8.kcommon.data.serializer.type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;

/**
 * A type adapter for bukkit worlds.
 * <p>
 * Adaptations are simply done in World to String and String to World form.
 */
public enum WorldAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (!World.class.isAssignableFrom(typeToken.getRawType()))
            return null;

        return (TypeAdapter<T>) new TypeAdapterWorld();
    }

    /**
     * A type adapter for bukkit {@link org.bukkit.World} instances
     */
    public static class TypeAdapterWorld extends TypeAdapter<World> {
        @Override
        public void write(JsonWriter jsonWriter, World world) throws IOException {
            if (world == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.value(world.getName());
        }

        @Override
        public World read(JsonReader jsonReader) throws IOException {
            String str = jsonReader.nextString();
            if (str == null)
                return null;

            return Bukkit.getWorld(str);
        }
    }
}
