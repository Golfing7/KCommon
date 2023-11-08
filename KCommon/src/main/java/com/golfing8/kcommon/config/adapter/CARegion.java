package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.region.CuboidRegion;
import com.golfing8.kcommon.struct.region.Region;
import org.bukkit.World;

import java.util.LinkedHashMap;
import java.util.Map;

public class CARegion implements ConfigAdapter<Region> {
    @Override
    public Class<Region> getAdaptType() {
        return Region.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Region toPOJO(ConfigPrimitive entry, FieldType actualType) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> map = (Map<String, Object>) entry.getPrimitive();
        if (map.containsKey("region-type")) {
            World world = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString((String) map.get("world")), World.class);
            return new CuboidRegion(
                    (double) map.get("min-x"),
                    (double) map.get("max-x"),
                    (double) map.get("min-y"),
                    (double) map.get("max-y"),
                    (double) map.get("min-z"),
                    (double) map.get("max-z"),
                    world
            );
        }
        return null;
    }

    @Override
    public ConfigPrimitive toPrimitive(Region region) {
        Map<String, Object> section = new LinkedHashMap<>();
        if (region instanceof CuboidRegion) {
            section.put("min-x", region.getMinimumXValue());
            section.put("min-y", region.getMinimumYValue());
            section.put("min-z", region.getMinimumZValue());
            section.put("max-x", region.getMaximumXValue());
            section.put("max-y", region.getMaximumYValue());
            section.put("max-z", region.getMaximumZValue());
            section.put("region-type", "CUBOID");
        }
        if (region.getWorld() != null) {
            section.put("world", region.getWorld().getName());
        }
        return ConfigPrimitive.ofMap(section);
    }
}
