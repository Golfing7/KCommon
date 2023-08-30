package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSInBlockDig;
import com.golfing8.kcommon.nms.struct.Position;
import net.minecraft.network.protocol.game.PacketPlayInBlockDig;

public class InBlockDig implements NMSInBlockDig {
    private final PacketPlayInBlockDig packet;

    public InBlockDig(PacketPlayInBlockDig packet){
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Position getPosition() {
        return new Position(packet.b().u(), packet.b().v(), packet.b().w());
    }

    @Override
    public DigType getDigType() {
        switch (packet.d()){
            case e:
                return DigType.DROP_ITEM;
            case d:
                return DigType.DROP_ALL;
            case f:
                return DigType.RELEASE;
            case c:
                return DigType.STOP;
            case b:
                return DigType.ABORT;
            case a:
                return DigType.START;
            case g:
                return DigType.SWAP_HANDS;
        }
        return null;
    }

}
