package com.golfing8.kcommon.nms.tileentities;

import org.bukkit.entity.EntityType;

/**
 * NMS access for a mob spawner
 */
public interface NMSMobSpawner extends NMSTileEntity {
    /**
     * Sets the spawn delay of the spawner
     *
     * @param delay the delay
     */
    void setSpawnDelay(int delay);

    /**
     * Gets the spawn delay of the spawner
     *
     * @return the spawn delay
     */
    int getSpawnDelay();

    /**
     * Sets the entity type of the spawner
     *
     * @param entityType the type
     */
    void setEntityType(EntityType entityType);

    /**
     * Gets the entity type of the spawner
     *
     * @return the type
     */
    EntityType getEntityType();
}
