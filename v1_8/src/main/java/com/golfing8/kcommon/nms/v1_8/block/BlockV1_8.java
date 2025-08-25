package com.golfing8.kcommon.nms.v1_8.block;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.block.NMSBlockData;
import net.minecraft.server.v1_8_R3.Block;

public class BlockV1_8 implements NMSBlock {
    protected final Block block;

    public BlockV1_8(Block block) {
        this.block = block;
    }

    @Override
    public Object getHandle() {
        return block;
    }

    @Override
    public NMSBlockData getBlockData() {
        return new BlockDataV1_8(block.getBlockData());
    }

    @Override
    public NMSBlockData fromLegacyData(int data) {
        return new BlockDataV1_8(block.fromLegacyData(data));
    }
}
