package com.golfing8.kcommon.nms.v1_17.block;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.block.NMSBlockData;
import net.minecraft.world.level.block.Block;

public class BlockV1_17 implements NMSBlock {
    protected final Block block;

    public BlockV1_17(Block block){
        this.block = block;
    }

    @Override
    public Object getHandle() {
        return block;
    }

    @Override
    public NMSBlockData getBlockData() {
        return new BlockDataV1_17(block.getBlockData());
    }

    @Override
    public NMSBlockData fromLegacyData(int data) {
        return new BlockDataV1_17(block.getBlockData());
    }
}
