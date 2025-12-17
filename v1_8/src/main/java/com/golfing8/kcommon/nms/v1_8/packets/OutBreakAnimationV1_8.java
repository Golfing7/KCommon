package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutBreakAnimation;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.struct.Position;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;

@AllArgsConstructor
public class OutBreakAnimationV1_8 implements NMSOutBreakAnimation {
    private static final FieldHandle<Integer> ID_HANDLE = FieldHandles.getHandle("a", PacketPlayOutBlockBreakAnimation.class);
    private static final FieldHandle<BlockPosition> POSITION_HANDLE = FieldHandles.getHandle("b", PacketPlayOutBlockBreakAnimation.class);
    private static final FieldHandle<Integer> STAGE_HANDLE = FieldHandles.getHandle("c", PacketPlayOutBlockBreakAnimation.class);

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
