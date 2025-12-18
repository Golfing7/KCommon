package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityEffect;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEffect;

/**
 * NMS 1.8 entity effect packet
 */
@AllArgsConstructor
public class OutEntityEffectV1_8 implements NMSOutEntityEffect {
    private final PacketPlayOutEntityEffect packet;

    @Override
    public Object getHandle() {
        return packet;
    }
}
