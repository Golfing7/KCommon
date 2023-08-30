package com.golfing8.kcommon.nms.chunks;

import com.golfing8.kcommon.nms.NMSObject;
import org.bukkit.Material;

public interface NMSChunkSection extends NMSObject {

    void setType(int x, int y, int z, Material material);
}
