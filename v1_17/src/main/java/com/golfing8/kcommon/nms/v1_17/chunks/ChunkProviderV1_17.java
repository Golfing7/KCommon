package com.golfing8.kcommon.nms.v1_17.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import net.minecraft.server.level.ChunkProviderServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftChunk;

public class ChunkProviderV1_17 implements NMSChunkProvider {
    private final ChunkProviderServer handle;

    public ChunkProviderV1_17(ChunkProviderServer server){
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
        return new ChunkV1_17(handle.getChunkAt(x, z, true));
    }

    @Override
    public void saveChunk(NMSChunk chunk) {
        //Can't do anything here.
    }

}
