package com.golfing8.kcommon.nms.v1_19.block;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.block.NMSBlockData;

public class Block implements NMSBlock {
    protected final net.minecraft.world.level.block.Block block;

    public Block(net.minecraft.world.level.block.Block block){
        this.block = block;
    }

    @Override
    public Object getHandle() {
        return block;
    }

    @Override
    public NMSBlockData getBlockData() {
        return new BlockData(block.m());
    }

    @Override
    public NMSBlockData fromLegacyData(int data) {
        return new BlockData(block.m());
    }
}
