package com.golfing8.kcommon.nms.packets;

import org.bukkit.World;
import org.bukkit.entity.Entity;

/**
 * Packet wrapper for using entities
 */
public interface NMSInUseEntity extends NMSPacket {
    /**
     * Gets the entity that was interacted with in the world
     *
     * @param world the world
     * @return the entity
     */
    Entity getInteractedEntity(World world);
}
