package com.golfing8.kcommon.nms.chunks;

import com.golfing8.kcommon.nms.NMSObject;

/**
 * NMS access to the server chunk provider
 */
public interface NMSChunkProvider extends NMSObject {
    /**
     * If the chunk provider is forcing chunks to load
     *
     * @return the chunk load
     */
    boolean isForceChunkLoad();

    /**
     * Sets the chunk provider to forceful chunk loading
     *
     * @param value force chunk loading
     */
    void setForceChunkLoad(boolean value);

    /**
     * Gets or creates the chunk at the coordinates
     *
     * @param x the chunk x
     * @param z the chunk z
     * @return the chunk
     */
    NMSChunk getOrCreateChunk(int x, int z);

    /**
     * Saves the given chunk
     *
     * @param chunk the chunk
     */
    void saveChunk(NMSChunk chunk);
}
