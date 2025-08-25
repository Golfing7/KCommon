package com.golfing8.kcommon.nms.v1_8.block;

import com.golfing8.kcommon.nms.block.NMSDispenser;
import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.world.NMSWorld;
import net.minecraft.server.v1_8_R3.BlockDispenser;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.SourceBlock;
import net.minecraft.server.v1_8_R3.World;

public class BlockDispenserV1_8 extends BlockV1_8 implements NMSDispenser {
    public BlockDispenserV1_8(BlockDispenser dispenser) {
        super(dispenser);
    }

    @Override
    public Direction getFacing(Position position, NMSWorld world) {
        SourceBlock sourceBlock = new SourceBlock((World) world.getHandle(), new BlockPosition(position.getX(), position.getY(), position.getZ()));

        return Direction.fromOrdinal(BlockDispenser.b(sourceBlock.f()).a());
    }
}
