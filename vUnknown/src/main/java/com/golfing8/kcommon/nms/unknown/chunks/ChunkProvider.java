package com.golfing8.kcommon.nms.unknown.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import org.bukkit.World;

public class ChunkProvider implements NMSChunkProvider {
    private final World handle;

    public ChunkProvider(World server){
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
        return new Chunk(handle.getChunkAt(x, z));
    }

    @Override
    public void saveChunk(NMSChunk chunk) {
        //Can't do anything here.
    }
}
