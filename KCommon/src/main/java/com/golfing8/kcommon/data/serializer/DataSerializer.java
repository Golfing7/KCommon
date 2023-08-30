package com.golfing8.kcommon.data.serializer;

import com.golfing8.kcommon.struct.Pair;
import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A utility class containing special serialization for certain objects.
 */
@UtilityClass
public final class DataSerializer {
    /**
     * Transformers for serialization.
     */
    private static final Map<Class<?>, Pair<Function<Object, JsonObject>, Function<JsonObject, Object>>> TRANSFORMERS = new HashMap<>();

    /**
     * Registers a transformer to the backing transformers map.
     *
     * @param type the type to register.
     * @param tojson its function for Object -> Json
     * @param fromjson its frunction for Json -> Object
     * @param <T> the type.
     */
    @SuppressWarnings("unchecked")
    private static <T> void registerTransformer(Class<T> type, Function<T, JsonObject> tojson, Function<JsonObject, T> fromjson) {
        TRANSFORMERS.put(type, new Pair<>((Function<Object, JsonObject>) tojson, (Function<JsonObject, Object>) fromjson));
        //Side note, aren't java generics amazing?
    }

    static {
        registerTransformer(Location.class, (loc) -> {
            JsonObject object = new JsonObject();
            object.addProperty("x", loc.getX());
            object.addProperty("y", loc.getY());
            object.addProperty("z", loc.getZ());

            object.addProperty("world", loc.getWorld().getName());
            object.addProperty("yaw", loc.getYaw());
            object.addProperty("pitch", loc.getYaw());
            return object;
        }, (json) -> {
            double x = json.get("x").getAsDouble();
            double y = json.get("y").getAsDouble();
            double z = json.get("z").getAsDouble();

            World world = Bukkit.getWorld(json.get("world").getAsString());
            float yaw = json.get("yaw").getAsFloat();
            float pitch = json.get("pitch").getAsFloat();

            return new Location(world, x, y, z, yaw, pitch);
        });
    }

    /**
     * Attempts to serialize the given object as json. If
     *
     * @param obj the object to serialize.
     * @return the object in json form.
     * @throws IllegalArgumentException if the object does not have support for serialization.
     */
    public static JsonObject serialize(Object obj) throws IllegalArgumentException {
        if(!TRANSFORMERS.containsKey(obj.getClass()))
            throw new IllegalArgumentException(String.format("Unregistered class type %s!", obj.getClass().getName()));

        return TRANSFORMERS.get(obj.getClass()).getA().apply(obj);
    }

    /**
     * Deserializes the given json object as the given type.
     *
     * @param clazz the class of the type.
     * @param object the json object.
     * @return the deserialized object.
     * @throws IllegalArgumentException if the object does not have support for serialization.
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(Class<T> clazz, JsonObject object) {
        if(!TRANSFORMERS.containsKey(clazz))
            throw new IllegalArgumentException(String.format("Unregistered class type %s!", clazz.getName()));

        return (T) TRANSFORMERS.get(clazz).getB().apply(object);
    }
}
