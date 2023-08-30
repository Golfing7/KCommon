package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Position;

import java.util.List;

public interface NMSOutMultiBlockChange extends NMSPacket{
    List<Position> getPositions();
}
