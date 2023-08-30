package com.golfing8.kcommon.nms.v1_17.block;

import com.golfing8.kcommon.nms.block.NMSBlockData;
import lombok.AllArgsConstructor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.IBlockData;

@AllArgsConstructor
public class BlockDataV1_17 implements NMSBlockData {
    private final IBlockData data;

    @Override
    public Object getHandle() {
        return data;
    }
}
