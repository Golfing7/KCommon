package com.golfing8.kcommon.nms.v1_8.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkSection;
import net.minecraft.server.v1_8_R3.Chunk;

public class ChunkV1_8 implements NMSChunk {
    private final Chunk chunk;

    public ChunkV1_8(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public Object getHandle() {
        return chunk;
    }

    @Override
    public NMSChunkSection getSection(int y) {
        return new ChunkSectionV1_8(chunk.getSections()[y]);
    }

    @Override
    public void clearTileEntities() {
        chunk.tileEntities.clear();
    }
}
