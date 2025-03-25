package com.golfing8.kcommon.nms.access;

import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;

public interface NMSMagicInventories {
    default String getAnvilRenameText(InventoryView inventory) {
        if (!(inventory.getTopInventory() instanceof AnvilInventory))
            return null;

        return ((AnvilInventory) inventory.getTopInventory()).getRenameText();
    }
}
