package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A config adapter for bukkit Locations.
 */
public class CAVector implements ConfigAdapter<Vector> {
    private static final double DEF_COORD = 0.0D;

    @Override
    public Class<Vector> getAdaptType() {
        return Vector.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Vector toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        if (entry.getPrimitive() instanceof String) {
            String[] split = ((String) entry.getPrimitive()).split(":");
            double x = Double.parseDouble(split[0]);
            double y = Double.parseDouble(split[1]);
            double z = Double.parseDouble(split[2]);
            return new Vector(x, y, z);
        }
        Map<String, Object> map = (Map<String, Object>) entry.getPrimitive();
        double x = (double) map.getOrDefault("x", DEF_COORD);
        double y = (double) map.getOrDefault("y", DEF_COORD);
        double z = (double) map.getOrDefault("z", DEF_COORD);
        return new Vector(x, y, z);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Vector object) {
        return ConfigPrimitive.ofString(object.getX() + ":" + object.getY() + ":" + object.getZ());
    }
}
