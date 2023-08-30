package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityEffect;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayOutRemoveEntityEffect;

@AllArgsConstructor
public class OutRemoveEntityEffect implements NMSOutEntityEffect {
    private final PacketPlayOutRemoveEntityEffect packet;
    @Override
    public Object getHandle() {
        return packet;
    }
}
