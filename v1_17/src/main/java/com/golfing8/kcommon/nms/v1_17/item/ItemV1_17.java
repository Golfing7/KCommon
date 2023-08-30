package com.golfing8.kcommon.nms.v1_17.item;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

public class ItemV1_17 implements NMSItem {
    private final Item item;

    public ItemV1_17(Item item) {
        this.item = item;
    }

    @Override
    public Object getHandle() {
        return item;
    }

    @Override
    public float getDestroySpeed(NMSItemStack itemStack, NMSBlock block) {
        return item.getDestroySpeed((ItemStack) itemStack.getHandle(), (IBlockData) block.getBlockData().getHandle());
    }

    @Override
    public NMSItemStack newStack(int amount) {
        return new ItemStackV1_17(new ItemStack(this.item, amount));
    }
}
