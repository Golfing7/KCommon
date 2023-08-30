package com.golfing8.kcommon.nms.v1_19.block;

import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.world.NMSWorld;
import com.golfing8.kcommon.nms.block.NMSDispenser;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SourceBlock;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockDispenser extends Block implements NMSDispenser {
    public BlockDispenser(net.minecraft.world.level.block.BlockDispenser dispenser){
        super(dispenser);
    }

    @Override
    public Direction getFacing(Position position, NMSWorld world) {
        SourceBlock sourceBlock = new SourceBlock((WorldServer) world.getHandle(), new BlockPosition(position.getX(), position.getY(), position.getZ()));

        IBlockData data = sourceBlock.e();

        return Direction.fromOrdinal(data.c(net.minecraft.world.level.block.BlockDispenser.a).d());
    }
}
