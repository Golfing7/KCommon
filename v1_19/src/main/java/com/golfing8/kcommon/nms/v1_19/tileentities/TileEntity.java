package com.golfing8.kcommon.nms.v1_19.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.v1_19.block.Block;
import net.minecraft.core.BlockPosition;

public class TileEntity implements NMSTileEntity {
    private final net.minecraft.world.level.block.entity.TileEntity tileEntity;

    public TileEntity(net.minecraft.world.level.block.entity.TileEntity tileEntity){
        this.tileEntity = tileEntity;
    }

    @Override
    public Object getHandle() {
        return tileEntity;
    }

    @Override
    public Position getPosition() {
        BlockPosition position = tileEntity.p();
        return new Position(position.u(), position.v(), position.w());
    }

    @Override
    public NMSBlock getBlock() {
        return new Block(tileEntity.q().b());
    }
}
