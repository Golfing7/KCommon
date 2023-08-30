package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityMetadata;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;

@AllArgsConstructor
public class OutEntityMetadataV1_17 implements NMSOutEntityMetadata {
    private final PacketPlayOutEntityMetadata packet;
    @Override
    public Object getHandle() {
        return packet;
    }
}
