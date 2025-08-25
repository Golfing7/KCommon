package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSInBlockDig;
import com.golfing8.kcommon.nms.struct.Position;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockDig;

public class InBlockDigV1_8 implements NMSInBlockDig {
    private final PacketPlayInBlockDig packet;

    public InBlockDigV1_8(PacketPlayInBlockDig packet) {
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Position getPosition() {
        return new Position(packet.a().getX(), packet.a().getY(), packet.a().getZ());
    }

    @Override
    public DigType getDigType() {
        switch (packet.c()) {
            case DROP_ITEM:
                return DigType.DROP_ITEM;
            case DROP_ALL_ITEMS:
                return DigType.DROP_ALL;
            case RELEASE_USE_ITEM:
                return DigType.RELEASE;
            case STOP_DESTROY_BLOCK:
                return DigType.STOP;
            case ABORT_DESTROY_BLOCK:
                return DigType.ABORT;
            case START_DESTROY_BLOCK:
                return DigType.START;
        }
        return null;
    }
}
