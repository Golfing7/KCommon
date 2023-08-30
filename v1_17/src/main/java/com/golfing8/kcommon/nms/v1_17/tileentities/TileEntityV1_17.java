package com.golfing8.kcommon.nms.v1_17.tileentities;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.v1_17.block.BlockV1_17;
import net.minecraft.world.level.block.entity.TileEntity;

public class TileEntityV1_17 implements NMSTileEntity {
    private final TileEntity tileEntity;

    public TileEntityV1_17(TileEntity tileEntity){
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
        return new BlockV1_17(tileEntity.getBlock().getBlock());
    }
}
