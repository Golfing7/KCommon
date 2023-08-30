package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSOutBreakAnimation;
import com.golfing8.kcommon.nms.struct.Position;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;

@AllArgsConstructor
public class OutBreakAnimation implements NMSOutBreakAnimation {
    private final PacketPlayOutBlockBreakAnimation packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public int getEntityID() {
        return packet.b();
    }

    @Override
    public int getBreakStage() {
        return packet.d();
    }

    @Override
    public Position getPosition() {
        BlockPosition blockPosition = packet.c();

        return new Position(blockPosition.u(), blockPosition.v(), blockPosition.w());
    }
}
