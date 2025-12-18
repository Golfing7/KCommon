package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutEntityStatus;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.world.NMSWorld;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.entity.Entity;

/**
 * NMS 1.8 entity status packet
 */
public class OutEntityStatusV1_8 implements NMSOutEntityStatus {
    private static final FieldHandle<Integer> entityHandle = FieldHandles.getHandle("a", PacketPlayOutEntityStatus.class);

    private static final FieldHandle<Byte> statusHandle = FieldHandles.getHandle("b", PacketPlayOutEntityStatus.class);

    private final PacketPlayOutEntityStatus packet;

    public OutEntityStatusV1_8(PacketPlayOutEntityStatus packet) {
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Entity getEntity(NMSWorld world) {
        WorldServer ws = (WorldServer) world.getHandle();
        return ws.a(entityHandle.get(packet)).getBukkitEntity();
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
