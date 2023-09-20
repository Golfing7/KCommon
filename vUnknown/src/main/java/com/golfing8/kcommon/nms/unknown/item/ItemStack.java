package com.golfing8.kcommon.nms.unknown.item;

import com.golfing8.kcommon.nms.item.NMSItemStack;

public class ItemStack implements NMSItemStack {
    private final org.bukkit.inventory.ItemStack stack;

    public ItemStack(org.bukkit.inventory.ItemStack itemStack){
        this.stack = itemStack;
    }

    @Override
    public String getI18DisplayName() {
        return stack.getI18NDisplayName();
    }

    @Override
    public Object getHandle() {
        return stack;
    }
}
