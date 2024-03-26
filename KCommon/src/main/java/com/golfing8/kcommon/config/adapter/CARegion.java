package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.region.CuboidRegion;
import com.golfing8.kcommon.struct.region.RectangleRegion;
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
            String regionType = (String) map.get("region-type");
            switch (regionType) {
                case "CUBOID":
                    return new CuboidRegion(
                            ((Number) map.get("min-x")).doubleValue(),
                            ((Number) map.get("max-x")).doubleValue(),
                            ((Number) map.get("min-y")).doubleValue(),
                            ((Number) map.get("max-y")).doubleValue(),
                            ((Number) map.get("min-z")).doubleValue(),
                            ((Number) map.get("max-z")).doubleValue(),
                            world);
                case "RECTANGLE":
                    return new RectangleRegion(
                            ((Number) map.get("min-x")).doubleValue(),
                            ((Number) map.get("max-x")).doubleValue(),
                            ((Number) map.get("min-z")).doubleValue(),
                            ((Number) map.get("max-z")).doubleValue(),
                            world
                    );
            }
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
        } else if (region instanceof RectangleRegion) {
            section.put("min-x", region.getMinimumXValue());
            section.put("min-z", region.getMinimumZValue());
            section.put("max-x", region.getMaximumXValue());
            section.put("max-z", region.getMaximumZValue());
            section.put("region-type", "RECTANGLE");
        }
        if (region.getWorld() != null) {
            section.put("world", region.getWorld().getName());
        }
        return ConfigPrimitive.ofMap(section);
    }
}
