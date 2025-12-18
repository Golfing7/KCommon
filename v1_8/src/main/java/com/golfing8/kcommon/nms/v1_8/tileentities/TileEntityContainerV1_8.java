package com.golfing8.kcommon.nms.v1_8.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntityContainer;
import net.minecraft.server.v1_8_R3.TileEntityContainer;
import org.bukkit.inventory.Inventory;

/**
 * NMS 1.8 container tile entity
 */
public class TileEntityContainerV1_8 extends TileEntityV1_8 implements NMSTileEntityContainer {
    private final TileEntityContainer tileEntity;

    public TileEntityContainerV1_8(TileEntityContainer tileEntity) {
        super(tileEntity);
        this.tileEntity = tileEntity;
    }

    @Override
    public Inventory getInventory() {
        return tileEntity.getOwner().getInventory();
    }
}
