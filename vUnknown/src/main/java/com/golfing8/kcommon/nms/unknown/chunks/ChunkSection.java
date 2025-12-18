package com.golfing8.kcommon.nms.unknown.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunkSection;
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

        section.getBlock(x, y, z).setType(material, false);
    }
}
