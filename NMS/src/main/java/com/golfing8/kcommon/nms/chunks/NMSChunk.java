package com.golfing8.kcommon.nms.chunks;

import com.golfing8.kcommon.nms.NMSObject;

public interface NMSChunk extends NMSObject {

    NMSChunkSection getSection(int y);

    void clearTileEntities();
}
