package com.golfing8.kcommon.nms.v1_19.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkSection;

public class Chunk implements NMSChunk {
    private final net.minecraft.world.level.chunk.Chunk chunk;

    public Chunk(net.minecraft.world.level.chunk.Chunk chunk){
        this.chunk = chunk;
    }

    @Override
    public Object getHandle() {
        return chunk;
    }

    @Override
    public NMSChunkSection getSection(int y) {
        return new ChunkSection(chunk, y);
    }

    @Override
    public void clearTileEntities() {
        chunk.i.clear();
    }
}
