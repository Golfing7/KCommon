package com.golfing8.kcommon.nms.block;

import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.world.NMSWorld;

public interface NMSDispenser extends NMSBlock{
    Direction getFacing(Position position, NMSWorld world);
}
