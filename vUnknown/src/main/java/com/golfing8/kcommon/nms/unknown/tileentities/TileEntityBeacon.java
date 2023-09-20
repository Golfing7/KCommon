package com.golfing8.kcommon.nms.unknown.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntityBeacon;
import org.bukkit.block.Beacon;

public class TileEntityBeacon extends TileEntity implements NMSTileEntityBeacon {
    private final Beacon tileEntity;

    public TileEntityBeacon(Beacon tileEntity){
        super(tileEntity);
        this.tileEntity = tileEntity;
    }
}
