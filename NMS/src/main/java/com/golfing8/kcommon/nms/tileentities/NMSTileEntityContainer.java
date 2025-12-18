package com.golfing8.kcommon.nms.tileentities;

import org.bukkit.inventory.Inventory;

/**
 * A tile entity container
 */
public interface NMSTileEntityContainer extends NMSTileEntity {
    /**
     * Gets the inventory of the container
     *
     * @return the inventory
     */
    Inventory getInventory();
}
