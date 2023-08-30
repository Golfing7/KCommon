package com.golfing8.kcommon.nms.chunks;

import com.golfing8.kcommon.nms.NMSObject;

public interface NMSChunkProvider extends NMSObject {
    boolean isForceChunkLoad();

    void setForceChunkLoad(boolean value);

    NMSChunk getOrCreateChunk(int x, int z);

    void saveChunk(NMSChunk chunk);
}
