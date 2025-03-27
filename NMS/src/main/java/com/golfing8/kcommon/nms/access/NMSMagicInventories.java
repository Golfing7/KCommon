package com.golfing8.kcommon.nms.access;

import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;

public interface NMSMagicInventories {
    default String getAnvilRepairName(InventoryView view) {
        return null;
    }
}
