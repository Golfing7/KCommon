package com.golfing8.kcommon.nms.v1_17.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkSection;
import net.minecraft.world.level.chunk.Chunk;

public class ChunkV1_17 implements NMSChunk {
    private final Chunk chunk;

    public ChunkV1_17(Chunk chunk){
        this.chunk = chunk;
    }

    @Override
    public Object getHandle() {
        return chunk;
    }

    @Override
    public NMSChunkSection getSection(int y) {
        return new ChunkSectionV1_17(chunk.getSections()[y]);
    }

    @Override
    public void clearTileEntities() {
        chunk.l.clear();
    }
}
