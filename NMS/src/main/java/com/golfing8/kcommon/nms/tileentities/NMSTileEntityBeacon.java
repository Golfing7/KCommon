package com.golfing8.kcommon.nms.tileentities;

import com.golfing8.kcommon.nms.NMSObject;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.struct.Position;

public interface NMSTileEntityBeacon extends NMSObject {
    Position getPosition();

    NMSBlock getBlock();
}