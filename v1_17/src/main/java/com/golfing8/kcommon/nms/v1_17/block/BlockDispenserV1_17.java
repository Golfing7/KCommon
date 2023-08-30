package com.golfing8.kcommon.nms.v1_17.block;

import com.golfing8.kcommon.nms.block.NMSDispenser;
import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.world.NMSWorld;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.SourceBlock;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.level.block.state.IBlockData;

public class BlockDispenserV1_17 extends BlockV1_17 implements NMSDispenser {
    public BlockDispenserV1_17(BlockDispenser dispenser){
        super(dispenser);
    }

    @Override
    public Direction getFacing(Position position, NMSWorld world) {
        SourceBlock sourceBlock = new SourceBlock((WorldServer) world.getHandle(), new BlockPosition(position.getX(), position.getY(), position.getZ()));

        IBlockData data = sourceBlock.getBlockData();

        return Direction.fromOrdinal(data.get(BlockDispenser.a).ordinal());
    }
}
