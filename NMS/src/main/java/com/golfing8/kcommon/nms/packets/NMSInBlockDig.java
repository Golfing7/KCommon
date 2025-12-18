package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Position;

/**
 * Packet wrapper for block digging
 */
public interface NMSInBlockDig extends NMSPacket {
    /**
     * Gets the position of this packet
     *
     * @return the position
     */
    Position getPosition();

    /**
     * Gets the dig type of the packet
     *
     * @return the dig type
     */
    DigType getDigType();

    /**
     * The type of dig that is being done
     */
    enum DigType {
        START,
        STOP,
        ABORT,
        DROP_ALL,
        DROP_ITEM,
        RELEASE,
        SWAP_HANDS,
        ;
    }
}
