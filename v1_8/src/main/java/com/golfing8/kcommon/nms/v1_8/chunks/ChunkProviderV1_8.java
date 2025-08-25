package com.golfing8.kcommon.nms.v1_8.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;

public class ChunkProviderV1_8 implements NMSChunkProvider {
    private final ChunkProviderServer handle;

    public ChunkProviderV1_8(ChunkProviderServer server) {
        this.handle = server;
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    @Override
    public boolean isForceChunkLoad() {
        return handle.forceChunkLoad;
    }

    @Override
    public void setForceChunkLoad(boolean value) {
        handle.forceChunkLoad = value;
    }

    @Override
    public NMSChunk getOrCreateChunk(int x, int z) {
        return new ChunkV1_8(handle.getOrCreateChunk(x, z));
    }

    @Override
    public void saveChunk(NMSChunk chunk) {
        handle.saveChunk((Chunk) chunk.getHandle());
    }
}
