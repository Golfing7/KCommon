package com.golfing8.kcommon.nms.tileentities;

import org.bukkit.inventory.Inventory;

public interface NMSTileEntityContainer extends NMSTileEntity{
    Inventory getInventory();
}
