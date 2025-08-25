package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;

public class InUseEntityV1_8 implements NMSInUseEntity {
    private final PacketPlayInUseEntity packet;

    public InUseEntityV1_8(PacketPlayInUseEntity packet) {
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Entity getInteractedEntity(World world) {
        net.minecraft.server.v1_8_R3.Entity a = packet.a(((CraftWorld) world).getHandle());
        return a == null ? null : a.getBukkitEntity();
    }
}
