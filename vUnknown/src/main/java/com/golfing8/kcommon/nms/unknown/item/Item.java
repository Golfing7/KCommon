package com.golfing8.kcommon.nms.unknown.item;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.block.NMSBlockData;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.unknown.block.BlockData;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class Item implements NMSItem {
    private final Material item;

    public Item(Material item) {
        this.item = item;
    }

    @Override
    public Object getHandle() {
        return item;
    }

    @Override
    public float getDestroySpeed(NMSItemStack itemStack, NMSBlock block) {
        Block b = (Block) block.getBlockData().getHandle();

        return b.getDestroySpeed((org.bukkit.inventory.ItemStack) itemStack.getHandle(), true);
    }

    @Override
    public NMSItemStack newStack(int amount) {
        return new ItemStack(new org.bukkit.inventory.ItemStack(item, amount));
    }
}
