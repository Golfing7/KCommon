package com.golfing8.kcommon.nms.v1_19.block;

import com.golfing8.kcommon.nms.block.NMSBlockData;
import lombok.AllArgsConstructor;
import net.minecraft.world.level.block.state.IBlockData;

@AllArgsConstructor
public class BlockData implements NMSBlockData {
    private final IBlockData data;

    @Override
    public Object getHandle() {
        return data;
    }
}
