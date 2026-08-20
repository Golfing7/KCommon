package com.golfing8.kcommon.nms.v26_1.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.util.FoliaSchedulers;
import org.bukkit.World;

/**
 * API agnostic chunk provider
 */
public class ChunkProvider implements NMSChunkProvider {
    private final World handle;

    public ChunkProvider(World server) {
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
        return FoliaSchedulers.ofProvidingPlugin(ChunkProvider.class).callAtChunkNow(handle, x, z, () -> new Chunk(handle.getChunkAt(x, z)));
    }

    @Override
    public void saveChunk(NMSChunk chunk) {
        //Can't do anything here.
    }
}
