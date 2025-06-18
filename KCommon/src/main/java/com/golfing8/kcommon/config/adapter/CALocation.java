package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A config adapter for bukkit Locations.
 */
public class CALocation implements ConfigAdapter<Location> {
    private static final double DEF_COORD = 0.0D;
    private static final float DEF_YAW = 0.0F;
    private static final float DEF_PITCH = 0.0F;

    @Override
    public Class<Location> getAdaptType() {
        return Location.class;
    }

    @Override
    public Location toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        // Check serialization type
        if (entry.getPrimitive() instanceof String) {
            String string = entry.getPrimitive().toString();
            String[] split = string.split("[:;]");
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            double z = Double.parseDouble(split[2]);
            World world = Bukkit.getWorld(split[3]);
            float yaw = 0.0F;
            float pitch = 0.0F;
            if (split.length == 6) {
                yaw = Float.parseFloat(split[4]);
                pitch = Float.parseFloat(split[5]);
            }
            return new Location(world, x, y, z, yaw, pitch);
        }

        Map<String, Object> map = entry.unwrap();
        String sWorld = (String) map.get("world");
        double x = (double) ConfigPrimitive.coerceObjectToBoxed(map.getOrDefault("x", DEF_COORD), Double.class);
        double y = (double) ConfigPrimitive.coerceObjectToBoxed(map.getOrDefault("y", DEF_COORD), Double.class);
        double z = (double) ConfigPrimitive.coerceObjectToBoxed(map.getOrDefault("z", DEF_COORD), Double.class);
        float yaw = (float) ConfigPrimitive.coerceObjectToBoxed(map.getOrDefault("yaw", DEF_COORD), Float.class);
        float pitch = (float) ConfigPrimitive.coerceObjectToBoxed(map.getOrDefault("pitch", DEF_COORD), Float.class);
        return new Location(Bukkit.getWorld(sWorld), x, y, z, yaw, pitch);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Location object) {
        StringBuilder locationSerialization = new StringBuilder();
        locationSerialization.append(object.getX()).append(";");
        locationSerialization.append(object.getY()).append(";");
        locationSerialization.append(object.getZ()).append(";");
        locationSerialization.append(object.getWorld().getName());
        if (object.getYaw() != DEF_YAW || object.getPitch() != DEF_PITCH) {
            locationSerialization.append(";").append(object.getYaw());
            locationSerialization.append(";").append(object.getPitch());
        }
        return ConfigPrimitive.ofString(locationSerialization.toString());
    }
}
