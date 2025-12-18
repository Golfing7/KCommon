package com.golfing8.kcommon.nms.packets;

/**
 * Packet wrapper for clicking a window
 */
public interface NMSInWindowClick extends NMSPacket {
    /**
     * Gets the clicked slot
     *
     * @return the slot
     */
    int getSlot();
}
