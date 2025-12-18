package com.golfing8.kcommon.nms.unknown.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntityContainer;
import org.bukkit.block.Container;
import org.bukkit.inventory.Inventory;

/**
 * API agnostic container tile entity
 */
public class TileEntityContainer extends TileEntity implements NMSTileEntityContainer {
    private final Container tileEntity;

    public TileEntityContainer(Container tileEntity) {
        super(tileEntity);
        this.tileEntity = tileEntity;
    }

    @Override
    public Inventory getInventory() {
        return tileEntity.getInventory();
    }
}
