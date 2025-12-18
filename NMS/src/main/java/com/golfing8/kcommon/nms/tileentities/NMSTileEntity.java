package com.golfing8.kcommon.nms.tileentities;

import com.golfing8.kcommon.nms.NMSObject;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.struct.Position;

/**
 * Abstract NMS tile entity
 */
public interface NMSTileEntity extends NMSObject {
    /**
     * Gets the position of the tile entity
     *
     * @return the position
     */
    Position getPosition();

    /**
     * Gets the block at the location
     *
     * @return the block
     */
    NMSBlock getBlock();
}
