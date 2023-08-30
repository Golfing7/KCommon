package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutSpawnEntityLiving;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

@AllArgsConstructor
public class OutSpawnEntityLivingV1_8 implements NMSOutSpawnEntityLiving {
    private final PacketPlayOutSpawnEntityLiving packet;
    @Override
    public Object getHandle() {
        return packet;
    }
}
