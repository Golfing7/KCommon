package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSInWindowClick;
import net.minecraft.network.protocol.game.PacketPlayInWindowClick;

public class InWindowClick implements NMSInWindowClick {
    private final PacketPlayInWindowClick packet;

    public InWindowClick(PacketPlayInWindowClick packet){
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
