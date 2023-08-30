package com.golfing8.kcommon.nms.v1_8.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.v1_8.block.BlockV1_8;
import net.minecraft.server.v1_8_R3.TileEntity;

public class TileEntityV1_8 implements NMSTileEntity {
    private final TileEntity tileEntity;

    public TileEntityV1_8(TileEntity tileEntity){
        this.tileEntity = tileEntity;
    }

    @Override
    public Object getHandle() {
        return tileEntity;
    }

    @Override
    public Position getPosition() {
        return new Position(tileEntity.getPosition().getX(), tileEntity.getPosition().getY(), tileEntity.getPosition().getZ());
    }

    @Override
    public NMSBlock getBlock() {
        return new BlockV1_8(tileEntity.w());
    }
}
