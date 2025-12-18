package com.golfing8.kcommon.nms.chunks;

import com.golfing8.kcommon.nms.NMSObject;

/**
 * NMS access for a chunk
 */
public interface NMSChunk extends NMSObject {

    /**
     * Gets the chunk section at the y index
     *
     * @param y the y
     * @return the chunk section
     */
    NMSChunkSection getSection(int y);

    /**
     * Clear all tile entities in the chunk
     */
    void clearTileEntities();
}
