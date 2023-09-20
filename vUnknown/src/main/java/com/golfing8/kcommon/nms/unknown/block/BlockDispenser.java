package com.golfing8.kcommon.nms.unknown.block;

import com.golfing8.kcommon.nms.block.NMSDispenser;
import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.world.NMSWorld;
import org.bukkit.block.Dispenser;

public class BlockDispenser extends Block implements NMSDispenser {
    public BlockDispenser(Dispenser dispenser){
        super(dispenser.getBlockData());
    }

    @Override
    public Direction getFacing(Position position, NMSWorld world) {
        org.bukkit.block.data.type.Dispenser dispenser = (org.bukkit.block.data.type.Dispenser) getHandle();
        return Direction.valueOf(dispenser.getFacing().name());
    }
}
