package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSInWindowClick;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;

public class InWindowClickV1_17 implements NMSInWindowClick {
    private final PacketPlayInWindowClick packet;

    public InWindowClickV1_17(PacketPlayInWindowClick packet){
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
