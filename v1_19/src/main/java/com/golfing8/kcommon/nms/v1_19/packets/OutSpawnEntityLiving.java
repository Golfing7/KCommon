package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSOutSpawnEntityLiving;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;

@AllArgsConstructor
public class OutSpawnEntityLiving implements NMSOutSpawnEntityLiving {
    private final PacketPlayOutSpawnEntity packet;
    @Override
    public Object getHandle() {
        return packet;
    }
}
