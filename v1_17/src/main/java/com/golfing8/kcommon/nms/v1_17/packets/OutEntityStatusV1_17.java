package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityStatus;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.world.NMSWorld;
import net.minecraft.network.protocol.game.PacketPlayOutEntityStatus;
import net.minecraft.server.level.WorldServer;
import org.bukkit.entity.Entity;

public class OutEntityStatusV1_17 implements NMSOutEntityStatus {
    private static final FieldHandle<Integer> entityHandle =
            (FieldHandle<Integer>) FieldHandles.getHandle("a", PacketPlayOutEntityStatus.class);

    private static final FieldHandle<Byte> statusHandle =
            (FieldHandle<Byte>) FieldHandles.getHandle("b", PacketPlayOutEntityStatus.class);

    private final PacketPlayOutEntityStatus packet;

    public OutEntityStatusV1_17(PacketPlayOutEntityStatus packet){
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Entity getEntity(NMSWorld world) {
        WorldServer ws = (WorldServer) world.getHandle();
        return ws.getEntity(entityHandle.get(packet)).getBukkitEntity();
    }

    @Override
    public int getEntityID() {
        return entityHandle.get(packet);
    }

    @Override
    public byte getCode() {
        return statusHandle.get(packet);
    }
}
