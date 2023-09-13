package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.Bukkit;
import org.bukkit.Location;

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
    @SuppressWarnings("unchecked")
    public Location toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> map = (Map<String, Object>) entry.getPrimitive();
        String sWorld = (String) map.get("world");
        double x = (double) map.getOrDefault("x", DEF_COORD);
        double y = (double) map.getOrDefault("y", DEF_COORD);
        double z = (double) map.getOrDefault("z", DEF_COORD);
        float yaw = (float) map.getOrDefault("yaw", DEF_YAW);
        float pitch = (float) map.getOrDefault("pitch", DEF_YAW);
        return new Location(Bukkit.getWorld(sWorld), x, y, z, yaw, pitch);
    }

    @Override
    public ConfigPrimitive toPrimitive(Location object) {
        Map<String, Object> data = new HashMap<>();
        data.put("world", object.getWorld().getName());
        data.put("x", object.getX());
        data.put("y", object.getY());
        data.put("z", object.getZ());
        if (object.getYaw() != DEF_YAW)
            data.put("yaw", object.getYaw());
        if (object.getPitch() != DEF_PITCH)
            data.put("pitch", object.getPitch());
        return ConfigPrimitive.ofMap(data);
    }
}
