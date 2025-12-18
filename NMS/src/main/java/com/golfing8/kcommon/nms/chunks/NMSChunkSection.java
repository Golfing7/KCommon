package com.golfing8.kcommon.nms.chunks;

import com.golfing8.kcommon.nms.NMSObject;
import org.bukkit.Material;

/**
 * NMS chunk section access
 */
public interface NMSChunkSection extends NMSObject {

    /**
     * Sets the type in the chunk section
     *
     * @param x the x
     * @param y the y
     * @param z the z
     * @param material the material
     */
    void setType(int x, int y, int z, Material material);
}
