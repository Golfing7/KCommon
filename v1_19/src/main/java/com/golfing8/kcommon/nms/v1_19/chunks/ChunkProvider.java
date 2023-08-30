package com.golfing8.kcommon.nms.v1_19.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import net.minecraft.server.level.ChunkProviderServer;

public class ChunkProvider implements NMSChunkProvider {
    private final ChunkProviderServer handle;

    public ChunkProvider(ChunkProviderServer server){
        this.handle = server;
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    @Override
    public boolean isForceChunkLoad() {
        return true;
    }

    @Override
    public void setForceChunkLoad(boolean value) {
        //handle.forceChunkLoad = value;
    }

    @Override
    public NMSChunk getOrCreateChunk(int x, int z) {
        return new Chunk(handle.a(x, z));
    }

    @Override
    public void saveChunk(NMSChunk chunk) {
        //Can't do anything here.
    }
}
