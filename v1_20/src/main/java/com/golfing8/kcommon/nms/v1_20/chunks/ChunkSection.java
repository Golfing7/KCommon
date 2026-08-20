package com.golfing8.kcommon.nms.v1_20.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunkSection;
import com.golfing8.kcommon.util.FoliaSchedulers;
import org.bukkit.Material;

/**
 * API agnostic chunk section
 */
public class ChunkSection implements NMSChunkSection {
    private final org.bukkit.Chunk section;
    private final int yShift;

    public ChunkSection(org.bukkit.Chunk section, int yShift) {
        this.section = section;
        this.yShift = yShift;
    }

    @Override
    public Object getHandle() {
        return section;
    }

    @Override
    public void setType(int x, int y, int z, Material material) {
        y += yShift * 16;

        if (section == null)
            return;

        final int adjustedY = y;
        FoliaSchedulers.ofProvidingPlugin(ChunkSection.class).callAtChunkNow(section.getWorld(), section.getX(), section.getZ(), () -> {
            section.getBlock(x, adjustedY, z).setType(material, false);
            return null;
        });
    }
}
