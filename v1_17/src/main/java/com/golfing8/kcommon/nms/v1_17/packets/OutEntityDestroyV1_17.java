package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityDestroy;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;

@AllArgsConstructor
public class OutEntityDestroyV1_17 implements NMSOutEntityDestroy {
    //Raw because primitive array -> wrapper array
    private static final FieldHandle handle = FieldHandles.getHandle("a", PacketPlayOutEntityDestroy.class);

    private final PacketPlayOutEntityDestroy packet;
    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public int[] getToDestroy() {
        return (int[]) handle.get(packet);
    }
}
