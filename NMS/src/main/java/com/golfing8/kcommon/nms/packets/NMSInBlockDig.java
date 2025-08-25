package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Position;

public interface NMSInBlockDig extends NMSPacket {
    Position getPosition();

    DigType getDigType();

    public static enum DigType {
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
