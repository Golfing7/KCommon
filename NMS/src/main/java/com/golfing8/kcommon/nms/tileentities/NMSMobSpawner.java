package com.golfing8.kcommon.nms.tileentities;

import org.bukkit.entity.EntityType;

public interface NMSMobSpawner extends NMSTileEntity{
    void setSpawnDelay(int delay);

    int getSpawnDelay();

    void setEntityType(EntityType entityType);

    EntityType getEntityType();
}
