package com.golfing8.kcommon.nms.unknown.tileentities;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.unknown.block.Block;
import org.bukkit.block.TileState;

/**
 * API agnostic tile entity
 */
public class TileEntity implements NMSTileEntity {
    private final TileState tileEntity;

    public TileEntity(TileState tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public Object getHandle() {
        return tileEntity;
    }

    @Override
    public Position getPosition() {
        return new Position(tileEntity.getBlock());
    }

    @Override
    public NMSBlock getBlock() {
        return new Block(tileEntity.getBlock().getBlockData());
    }
}
