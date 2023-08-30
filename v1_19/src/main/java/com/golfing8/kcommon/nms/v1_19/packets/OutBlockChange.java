package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSOutBlockChange;
import com.golfing8.kcommon.nms.struct.Position;
import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;

@AllArgsConstructor
public class OutBlockChange implements NMSOutBlockChange {
    private final PacketPlayOutBlockChange packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Position getPosition() {
        BlockPosition blockPosition = packet.c();
        return new Position(blockPosition.u(), blockPosition.v(), blockPosition.w());
    }
}
