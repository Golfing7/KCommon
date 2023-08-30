package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityEffect;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEffect;

@AllArgsConstructor
public class OutEntityEffect implements NMSOutEntityEffect {
    private final PacketPlayOutEntityEffect packet;
    @Override
    public Object getHandle() {
        return packet;
    }
}
