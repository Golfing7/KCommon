package com.golfing8.kcommon.nms.v1_19.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunkSection;
import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.chunk.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;

public class ChunkSection implements NMSChunkSection {
    private final Chunk section;
    private final int yShift;

    public ChunkSection(Chunk section, int yShift){
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

        if(section == null)
            return;

        section.setBlockState(new BlockPosition(x, y, z), CraftMagicNumbers.getBlock(material).m(), false, false);
    }
}
