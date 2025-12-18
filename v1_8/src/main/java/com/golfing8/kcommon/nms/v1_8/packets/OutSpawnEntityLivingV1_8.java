package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutSpawnEntityLiving;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

/**
 * NMS 1.8 spawn entity living packet
 */
@AllArgsConstructor
public class OutSpawnEntityLivingV1_8 implements NMSOutSpawnEntityLiving {
    private final PacketPlayOutSpawnEntityLiving packet;

    @Override
    public Object getHandle() {
        return packet;
    }
}
