package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Position;

/**
 * Packet wrapper for changing a block
 */
public interface NMSOutBlockChange extends NMSPacket {
    /**
     * Gets the position of the block
     *
     * @return the block
     */
    Position getPosition();
}
