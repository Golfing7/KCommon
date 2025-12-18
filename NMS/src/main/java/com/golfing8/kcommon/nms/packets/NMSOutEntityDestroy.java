package com.golfing8.kcommon.nms.packets;

/**
 * Packet wrapper for entity destroy packet
 */
public interface NMSOutEntityDestroy extends NMSPacket {
    /**
     * Gets the entity ids to destroy
     *
     * @return the ids
     */
    int[] getToDestroy();
}
