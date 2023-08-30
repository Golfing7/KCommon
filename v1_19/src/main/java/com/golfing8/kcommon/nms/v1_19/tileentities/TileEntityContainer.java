package com.golfing8.kcommon.nms.v1_19.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntityContainer;
import org.bukkit.inventory.Inventory;

public class TileEntityContainer extends TileEntity implements NMSTileEntityContainer {
    private final net.minecraft.world.level.block.entity.TileEntityContainer tileEntity;

    public TileEntityContainer(net.minecraft.world.level.block.entity.TileEntityContainer tileEntity){
        super(tileEntity);
        this.tileEntity = tileEntity;
    }

    @Override
    public Inventory getInventory() {
        return tileEntity.getOwner().getInventory();
    }
}
