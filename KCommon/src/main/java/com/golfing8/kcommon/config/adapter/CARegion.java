package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.region.CuboidRegion;
import com.golfing8.kcommon.struct.region.Region;

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
            return new CuboidRegion(
                    (double) map.get("min-x"),
                    (double) map.get("max-x"),
                    (double) map.get("min-y"),
                    (double) map.get("max-y"),
                    (double) map.get("min-z"),
                    (double) map.get("max-z")
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
        return ConfigPrimitive.ofMap(section);
    }
}
