package com.golfing8.kcommon.struct.item;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * An item handle mapping to an inventory and slot
 */
@AllArgsConstructor
public class InventoryItemHandle implements ItemHandle {
    private final Inventory inventory;
    private final int slot;

    @Override
    public ItemStack get() {
        return inventory.getItem(slot);
    }

    @Override
    public void set(ItemStack item) {
        inventory.setItem(slot, item);
    }
}
