package com.golfing8.kcommon.nms.packets;

import org.bukkit.inventory.ItemStack;

/**
 * Packet wrapper for item setting in creative
 */
public interface NMSInSetCreativeSlot extends NMSPacket {
    /**
     * The item being set
     *
     * @return the item
     */
    ItemStack getItemStack();

    /**
     * The slot it's being set in
     *
     * @return the slot
     */
    int getSlot();
}
