package com.golfing8.kcommon.nms.v1_8.item;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import net.minecraft.server.v1_8_R3.Block;
import net.minecraft.server.v1_8_R3.Item;
import net.minecraft.server.v1_8_R3.ItemStack;

public class ItemV1_8 implements NMSItem {
    private final Item item;

    public ItemV1_8(Item item) {
        this.item = item;
    }

    @Override
    public Object getHandle() {
        return item;
    }

    @Override
    public float getDestroySpeed(NMSItemStack itemStack, NMSBlock block) {
        return item.getDestroySpeed((ItemStack) itemStack.getHandle(), (Block) block.getHandle());
    }

    @Override
    public NMSItemStack newStack(int amount) {
        return new ItemStackV1_8(new ItemStack(this.item, amount));
    }
}
