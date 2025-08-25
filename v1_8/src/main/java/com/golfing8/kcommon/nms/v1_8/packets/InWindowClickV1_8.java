package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSInWindowClick;
import net.minecraft.server.v1_8_R3.PacketPlayInWindowClick;

public class InWindowClickV1_8 implements NMSInWindowClick {
    private final PacketPlayInWindowClick packet;

    public InWindowClickV1_8(PacketPlayInWindowClick packet) {
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public int getSlot() {
        return packet.b();
    }
}
