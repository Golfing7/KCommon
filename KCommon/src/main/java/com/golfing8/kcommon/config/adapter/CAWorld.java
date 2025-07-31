package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * A config adapter for bukkit worlds.
 */
public class CAWorld implements ConfigAdapter<World> {
    @Override
    public Class<World> getAdaptType() {
        return World.class;
    }

    @Override
    public World toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return Bukkit.getWorld((String) entry.getPrimitive());
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull World object) {
        return ConfigPrimitive.ofString(object.getName());
    }
}
