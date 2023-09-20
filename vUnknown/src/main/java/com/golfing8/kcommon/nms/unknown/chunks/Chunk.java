package com.golfing8.kcommon.nms.unknown.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkSection;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

public class Chunk implements NMSChunk {
    private final org.bukkit.Chunk chunk;

    public Chunk(org.bukkit.Chunk chunk){
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
        for (BlockState state : chunk.getTileEntities()) {
            chunk.getBlock(state.getX(), state.getY(), state.getZ()).setType(Material.AIR);
        }
    }
}
