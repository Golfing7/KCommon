package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSOutBreakAnimation;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.struct.Position;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;

@AllArgsConstructor
public class OutBreakAnimationV1_17 implements NMSOutBreakAnimation {
    private static final FieldHandle<Integer> ID_HANDLE =
            (FieldHandle<Integer>) FieldHandles.getHandle("a", PacketPlayOutBlockBreakAnimation.class);
    private static final FieldHandle<BlockPosition> POSITION_HANDLE =
            (FieldHandle<BlockPosition>) FieldHandles.getHandle("b", PacketPlayOutBlockBreakAnimation.class);
    private static final FieldHandle<Integer> STAGE_HANDLE =
            (FieldHandle<Integer>) FieldHandles.getHandle("c", PacketPlayOutBlockBreakAnimation.class);

    private final PacketPlayOutBlockBreakAnimation packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public int getEntityID() {
        return ID_HANDLE.get(packet);
    }

    @Override
    public int getBreakStage() {
        return STAGE_HANDLE.get(packet);
    }

    @Override
    public Position getPosition() {
        BlockPosition blockPosition = POSITION_HANDLE.get(packet);

        return new Position(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
    }
}
