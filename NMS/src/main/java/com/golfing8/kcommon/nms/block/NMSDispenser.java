package com.golfing8.kcommon.nms.block;

import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.world.NMSWorld;

/**
 * NMS binding for dispensers
 */
public interface NMSDispenser extends NMSBlock {
    /**
     * Gets the facing of the dispenser at the position
     *
     * @param position the position
     * @param world the world
     * @return the direction
     */
    Direction getFacing(Position position, NMSWorld world);
}
