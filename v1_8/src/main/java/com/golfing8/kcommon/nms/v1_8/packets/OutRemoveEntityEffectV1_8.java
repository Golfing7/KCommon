package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityEffect;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayOutRemoveEntityEffect;

/**
 * NMS 1.8 remove entity effect packet
 */
@AllArgsConstructor
public class OutRemoveEntityEffectV1_8 implements NMSOutEntityEffect {
    private final PacketPlayOutRemoveEntityEffect packet;

    @Override
    public Object getHandle() {
        return packet;
    }
}
