package com.golfing8.kcommon.nms.v1_19.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntityBeacon;

public class TileEntityBeacon extends TileEntity implements NMSTileEntityBeacon {
    private final net.minecraft.world.level.block.entity.TileEntityBeacon tileEntity;

    public TileEntityBeacon(net.minecraft.world.level.block.entity.TileEntityBeacon tileEntity){
        super(tileEntity);
        this.tileEntity = tileEntity;
    }
}
