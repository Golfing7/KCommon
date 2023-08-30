package com.golfing8.kcommon.nms.v1_17.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntityBeacon;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntityBeacon;
import net.minecraft.world.level.block.entity.TileEntityBeacon;

public class TileEntityBeaconV1_17 extends TileEntityV1_17 implements NMSTileEntityBeacon {
    private final TileEntityBeacon tileEntity;

    public TileEntityBeaconV1_17(TileEntityBeacon tileEntity){
        super(tileEntity);
        this.tileEntity = tileEntity;
    }
}
