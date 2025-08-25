package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityMetadata;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;

@AllArgsConstructor
public class OutEntityMetadataV1_8 implements NMSOutEntityMetadata {
    private final PacketPlayOutEntityMetadata packet;

    @Override
    public Object getHandle() {
        return packet;
    }
}
