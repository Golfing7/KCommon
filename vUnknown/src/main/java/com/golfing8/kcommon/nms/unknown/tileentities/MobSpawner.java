package com.golfing8.kcommon.nms.unknown.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSMobSpawner;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;

/**
 * API agnostic mob spawner
 */
public class MobSpawner extends TileEntity implements NMSMobSpawner {
    private final CreatureSpawner handle;

    public MobSpawner(CreatureSpawner handle) {
        super(handle);
        this.handle = handle;
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    @Override
    public void setSpawnDelay(int delay) {
        handle.setDelay(delay);
    }

    @Override
    public int getSpawnDelay() {
        return handle.getDelay();
    }

    @Override
    public void setEntityType(EntityType entityType) {
        handle.setSpawnedType(entityType);
    }

    @Override
    public EntityType getEntityType() {
        return handle.getSpawnedType();
    }
}
