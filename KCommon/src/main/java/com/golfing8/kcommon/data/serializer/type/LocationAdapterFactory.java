package com.golfing8.kcommon.data.serializer.type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.IOException;

/**
 * A type adapter for bukkit locations.
 */
public enum LocationAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (!Location.class.isAssignableFrom(typeToken.getRawType()))
            return null;

        return (TypeAdapter<T>) new TypeAdapterWorld();
    }

    public static class TypeAdapterWorld extends TypeAdapter<Location> {
        @Override
        public void write(JsonWriter jsonWriter, Location location) throws IOException {
            if (location == null) {
                jsonWriter.nullValue();
                return;
            }
            jsonWriter.beginObject();
            jsonWriter.name("x").value(location.getX());
            jsonWriter.name("y").value(location.getY());
            jsonWriter.name("z").value(location.getZ());
            jsonWriter.name("yaw").value(location.getYaw());
            jsonWriter.name("pitch").value(location.getPitch());
            jsonWriter.name("world").value(location.getWorld().getName());
            jsonWriter.endObject();
        }

        @Override
        public Location read(JsonReader jsonReader) throws IOException {
            if (jsonReader.peek() == JsonToken.NULL)
                return null;

            double x = 0, y = 0, z = 0;
            double yaw = 0, pitch = 0;
            String world = null;
            jsonReader.beginObject();
            while (jsonReader.peek() != JsonToken.END_OBJECT) {
                String name = jsonReader.nextName();
                if (jsonReader.peek() == JsonToken.STRING) {
                    world = jsonReader.nextString();
                    continue;
                }

                switch (name) {
                    case "x":
                        x = jsonReader.nextDouble();
                        break;
                    case "y":
                        y = jsonReader.nextDouble();
                        break;
                    case "z":
                        z = jsonReader.nextDouble();
                        break;
                    case "yaw":
                        yaw = jsonReader.nextDouble();
                        break;
                    case "pitch":
                        pitch = jsonReader.nextDouble();
                        break;
                }
            }
            jsonReader.endObject();
            return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
        }
    }
}
