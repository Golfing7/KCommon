package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.MapUtil;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CABlockVector implements ConfigAdapter<BlockVector> {
    @Override
    public Class<BlockVector> getAdaptType() {
        return BlockVector.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BlockVector toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> map = (Map<String, Object>) entry.getPrimitive();
        return new BlockVector((int) map.getOrDefault("x", 0),
                (int) map.getOrDefault("y", 0),
                (int) map.getOrDefault("z", 0));
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull BlockVector object) {
        return ConfigPrimitive.ofMap(MapUtil.of("x", object.getX(),
                "y", object.getY(),
                "z", object.getZ()));
    }
}
