package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Position;

/**
 * Packet wrapper for break animations
 */
public interface NMSOutBreakAnimation extends NMSPacket {
    /**
     * Gets the entity ID breaking the block
     *
     * @return the entity ID
     */
    int getEntityID();

    /**
     * Gets the break stage of the packet
     *
     * @return the break stage
     */
    int getBreakStage();

    /**
     * Gets the position of the packet
     *
     * @return the position
     */
    Position getPosition();
}
