package com.golfing8.kcommon.nms.v26_1.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunk;
import com.golfing8.kcommon.nms.chunks.NMSChunkSection;
import com.golfing8.kcommon.util.FoliaSchedulers;
import org.bukkit.Material;
import org.bukkit.block.BlockState;

/**
 * API agnostic chunk
 */
public class Chunk implements NMSChunk {
    private final org.bukkit.Chunk chunk;

    public Chunk(org.bukkit.Chunk chunk) {
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
        FoliaSchedulers.ofProvidingPlugin(Chunk.class).callAtChunkNow(chunk.getWorld(), chunk.getX(), chunk.getZ(), () -> {
            for (BlockState state : chunk.getTileEntities()) {
                chunk.getBlock(state.getX(), state.getY(), state.getZ()).setType(Material.AIR);
            }
            return null;
        });
    }
}
