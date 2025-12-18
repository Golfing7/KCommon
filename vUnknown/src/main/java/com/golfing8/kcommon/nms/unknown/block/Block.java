package com.golfing8.kcommon.nms.unknown.block;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.block.NMSBlockData;

/**
 * Modern API binding for nms blocks
 */
public class Block implements NMSBlock {
    protected final org.bukkit.block.data.BlockData block;

    public Block(org.bukkit.block.data.BlockData block) {
        this.block = block;
    }

    @Override
    public Object getHandle() {
        return block;
    }

    @Override
    public NMSBlockData getBlockData() {
        return new BlockData(block);
    }

    @Override
    public NMSBlockData fromLegacyData(int data) {
        return new BlockData(block);
    }
}
