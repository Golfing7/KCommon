package com.golfing8.kcommon.nms.v1_17.item;

import com.golfing8.kcommon.nms.item.NMSItemStack;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

public class ItemStackV1_17 implements NMSItemStack {
    private final ItemStack stack;

    public ItemStackV1_17(ItemStack itemStack){
        this.stack = itemStack;
    }

    @Override
    public String getI18DisplayName() {
        return stack.n();
    }

    @Override
    public Object getHandle() {
        return stack;
    }
}
