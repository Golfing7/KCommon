package com.golfing8.kcommon.nms.v1_8.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntityBeacon;
import net.minecraft.server.v1_8_R3.TileEntityBeacon;

/**
 * NMS 1.8 beacon tile entity
 */
public class TileEntityBeaconV1_8 extends TileEntityContainerV1_8 implements NMSTileEntityBeacon {
    private final TileEntityBeacon tileEntity;

    public TileEntityBeaconV1_8(TileEntityBeacon tileEntity) {
        super(tileEntity);
        this.tileEntity = tileEntity;
    }
}
