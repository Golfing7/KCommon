package com.golfing8.kcommon.nms.packets;

import org.bukkit.inventory.ItemStack;

public interface NMSInSetCreativeSlot extends NMSPacket{
    ItemStack getItemStack();

    int getSlot();
}
