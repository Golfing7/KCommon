package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSOutSpawnEntity;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;

@AllArgsConstructor
public class OutSpawnEntityV1_17 implements NMSOutSpawnEntity {
    private final PacketPlayOutSpawnEntity packet;
    @Override
    public Object getHandle() {
        return packet;
    }
}
