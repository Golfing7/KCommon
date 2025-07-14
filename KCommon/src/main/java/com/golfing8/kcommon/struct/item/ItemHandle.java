package com.golfing8.kcommon.struct.item;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a handle for a bukkit item.
 */
public interface ItemHandle {
    /**
     * Gets the item
     *
     * @return the item
     */
    ItemStack get();

    /**
     * Sets the item
     *
     * @param item the item
     */
    void set(ItemStack item);
}
