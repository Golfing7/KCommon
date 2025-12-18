package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutSpawnEntity;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;

/**
 * NMS 1.8 spawn entity packet
 */
@AllArgsConstructor
public class OutSpawnEntityV1_8 implements NMSOutSpawnEntity {
    private final PacketPlayOutSpawnEntity packet;

    @Override
    public Object getHandle() {
        return packet;
    }
}
