package com.golfing8.kcommon.nms.v1_19.item;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import net.minecraft.world.level.block.state.IBlockData;

public class Item implements NMSItem {
    private final net.minecraft.world.item.Item item;

    public Item(net.minecraft.world.item.Item item) {
        this.item = item;
    }

    @Override
    public Object getHandle() {
        return item;
    }

    @Override
    public float getDestroySpeed(NMSItemStack itemStack, NMSBlock block) {
        return item.a((net.minecraft.world.item.ItemStack) itemStack.getHandle(), (IBlockData) block.getBlockData().getHandle());
    }

    @Override
    public NMSItemStack newStack(int amount) {
        return new ItemStack(new net.minecraft.world.item.ItemStack(this.item, amount));
    }
}
