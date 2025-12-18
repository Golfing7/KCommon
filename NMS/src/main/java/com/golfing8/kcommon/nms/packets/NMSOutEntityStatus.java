package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.world.NMSWorld;
import org.bukkit.entity.Entity;

/**
 * Packet wrapper for entity status
 */
public interface NMSOutEntityStatus extends NMSPacket {
    /**
     * Gets the entity in the world
     *
     * @param world the world
     * @return the entity
     */
    Entity getEntity(NMSWorld world);

    /**
     * Gets the entity ID in the packet
     *
     * @return the entity id
     */
    int getEntityID();

    /**
     * Gets the code of the status
     *
     * @return the code
     */
    byte getCode();
}
