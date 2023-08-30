package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Position;

public interface NMSOutBreakAnimation extends NMSPacket{
    int getEntityID();

    int getBreakStage();

    Position getPosition();
}
