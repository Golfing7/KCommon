package com.golfing8.kcommon.data.serializer;

import com.golfing8.kcommon.data.serializer.type.CooldownMapAdapterFactory;
import com.golfing8.kcommon.data.serializer.type.ItemStackAdapterFactory;
import com.golfing8.kcommon.data.serializer.type.LocationAdapterFactory;
import com.golfing8.kcommon.data.serializer.type.WorldAdapterFactory;
import com.golfing8.kcommon.struct.map.CooldownMap;
import com.golfing8.kcommon.struct.region.CuboidRegion;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

/**
 * The Bukkit types {@link DataSerializer} knows about, found through {@link java.util.ServiceLoader}.
 */
public class BukkitDataSerializerExtension implements DataSerializerExtension {
    @Override
    public void configure(GsonBuilder builder) {
        builder.registerTypeAdapterFactory(LocationAdapterFactory.INSTANCE);
        builder.registerTypeAdapterFactory(WorldAdapterFactory.INSTANCE);
        builder.registerTypeHierarchyAdapter(ItemStack.class, ItemStackAdapterFactory.INSTANCE);
        builder.registerTypeHierarchyAdapter(CooldownMap.class, CooldownMapAdapterFactory.INSTANCE);
    }

    @Override
    public void registerTransformers() {
        DataSerializer.registerTransformer(Location.class, loc -> {
            JsonObject object = new JsonObject();
            object.addProperty("x", loc.getX());
            object.addProperty("y", loc.getY());
            object.addProperty("z", loc.getZ());

            object.addProperty("world", loc.getWorld().getName());
            object.addProperty("yaw", loc.getYaw());
            object.addProperty("pitch", loc.getYaw());
            return object;
        }, json -> {
            double x = json.get("x").getAsDouble();
            double y = json.get("y").getAsDouble();
            double z = json.get("z").getAsDouble();

            World world = Bukkit.getWorld(json.get("world").getAsString());
            float yaw = json.get("yaw").getAsFloat();
            float pitch = json.get("pitch").getAsFloat();

            return new Location(world, x, y, z, yaw, pitch);
        });

        DataSerializer.registerTransformer(CuboidRegion.class, loc -> {
            JsonObject object = new JsonObject();
            object.addProperty("min-x", loc.getMinimumXValue());
            object.addProperty("min-y", loc.getMinimumYValue());
            object.addProperty("min-z", loc.getMinimumZValue());

            object.addProperty("max-x", loc.getMaximumXValue());
            object.addProperty("max-y", loc.getMaximumYValue());
            object.addProperty("max-z", loc.getMaximumZValue());
            return object;
        }, json -> {
            double minX = json.get("min-x").getAsDouble();
            double minY = json.get("min-y").getAsDouble();
            double minZ = json.get("min-z").getAsDouble();

            double maxX = json.get("max-x").getAsDouble();
            double maxY = json.get("max-y").getAsDouble();
            double maxZ = json.get("max-z").getAsDouble();
            return new CuboidRegion(minX, maxX, minY, maxY, minZ, maxZ);
        });
    }
}
