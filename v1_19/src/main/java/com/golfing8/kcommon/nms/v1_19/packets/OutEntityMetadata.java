package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityMetadata;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;

@AllArgsConstructor
public class OutEntityMetadata implements NMSOutEntityMetadata {
    private final PacketPlayOutEntityMetadata packet;
    @Override
    public Object getHandle() {
        return packet;
    }
}
