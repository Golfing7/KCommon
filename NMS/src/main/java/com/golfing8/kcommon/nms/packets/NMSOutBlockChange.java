package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Position;

public interface NMSOutBlockChange extends NMSPacket {
    Position getPosition();
}
