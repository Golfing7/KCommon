package com.golfing8.kcommon.nms.v1_17.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntityContainer;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntityContainer;
import net.minecraft.world.level.block.entity.TileEntityContainer;
import org.bukkit.inventory.Inventory;

public class TileEntityContainerV1_17 extends TileEntityV1_17 implements NMSTileEntityContainer {
    private final TileEntityContainer tileEntity;

    public TileEntityContainerV1_17(TileEntityContainer tileEntity){
        super(tileEntity);
        this.tileEntity = tileEntity;
    }

    @Override
    public Inventory getInventory() {
        return tileEntity.getOwner().getInventory();
    }
}
