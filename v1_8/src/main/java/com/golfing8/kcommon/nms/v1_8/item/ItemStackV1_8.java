package com.golfing8.kcommon.nms.v1_8.item;

import com.golfing8.kcommon.nms.item.NMSItemStack;
import net.minecraft.server.v1_8_R3.ItemStack;

public class ItemStackV1_8 implements NMSItemStack {
    private final ItemStack stack;

    public ItemStackV1_8(ItemStack itemStack) {
        this.stack = itemStack;
    }

    @Override
    public String getI18DisplayName() {
        return stack.getItem().a(stack);
    }

    @Override
    public Object getHandle() {
        return stack;
    }
}
