package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSOutSpawnEntityLiving;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityLiving;

@AllArgsConstructor
public class OutSpawnEntityLivingV1_17 implements NMSOutSpawnEntityLiving {
    private final PacketPlayOutSpawnEntityLiving packet;
    @Override
    public Object getHandle() {
        return packet;
    }
}
