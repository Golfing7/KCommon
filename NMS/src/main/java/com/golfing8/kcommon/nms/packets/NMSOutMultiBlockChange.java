package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Position;

import java.util.List;

/**
 * Packet wrapper for multi block change
 */
public interface NMSOutMultiBlockChange extends NMSPacket {
    /**
     * The positions being changed
     *
     * @return the positions
     */
    List<Position> getPositions();
}
