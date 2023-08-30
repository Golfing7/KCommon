package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSOutBlockChange;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.struct.Position;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;

@AllArgsConstructor
public class OutBlockChangeV1_17 implements NMSOutBlockChange {
    private static final FieldHandle<BlockPosition> POSITION_FIELD =
            (FieldHandle<BlockPosition>) FieldHandles.getHandle("a", PacketPlayOutBlockChange.class);

    private final PacketPlayOutBlockChange packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Position getPosition() {
        BlockPosition blockPosition = POSITION_FIELD.get(packet);
        return new Position(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }
}
