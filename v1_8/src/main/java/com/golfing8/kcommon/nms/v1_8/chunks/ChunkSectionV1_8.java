package com.golfing8.kcommon.nms.v1_8.chunks;

import com.golfing8.kcommon.nms.chunks.NMSChunkSection;
import net.minecraft.server.v1_8_R3.ChunkSection;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;

public class ChunkSectionV1_8 implements NMSChunkSection {
    private final ChunkSection section;

    public ChunkSectionV1_8(ChunkSection section){
        this.section = section;
    }

    @Override
    public Object getHandle() {
        return section;
    }

    @Override
    public void setType(int x, int y, int z, Material material) {
        if(section == null)
            return;

        section.setType(x, y, z, CraftMagicNumbers.getBlock(material).getBlockData());
    }
}
