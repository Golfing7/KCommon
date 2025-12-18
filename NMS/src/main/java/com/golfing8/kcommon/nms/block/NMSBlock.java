package com.golfing8.kcommon.nms.block;

import com.golfing8.kcommon.nms.NMSObject;

/**
 * NMS bindings for blocks
 */
public interface NMSBlock extends NMSObject {

    /**
     * Gets default block data from this block
     *
     * @return the block data
     */
    NMSBlockData getBlockData();

    /**
     * Gets block data from the given legacy data
     *
     * @param data the data
     * @return the block data
     */
    NMSBlockData fromLegacyData(int data);
}
