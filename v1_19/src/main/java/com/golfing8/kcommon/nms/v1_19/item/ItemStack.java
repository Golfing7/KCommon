package com.golfing8.kcommon.nms.v1_19.item;

import com.golfing8.kcommon.nms.item.NMSItemStack;

public class ItemStack implements NMSItemStack {
    private final net.minecraft.world.item.ItemStack stack;

    public ItemStack(net.minecraft.world.item.ItemStack itemStack){
        this.stack = itemStack;
    }

    @Override
    public Object getHandle() {
        return stack;
    }
}
