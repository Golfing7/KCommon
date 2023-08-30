package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSInUseEntity;
import net.minecraft.network.protocol.game.PacketPlayInUseEntity;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.Entity;

public class InUseEntity implements NMSInUseEntity {
    private final PacketPlayInUseEntity packet;

    public InUseEntity(PacketPlayInUseEntity packet){
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Entity getInteractedEntity(World world) {
        net.minecraft.world.entity.Entity a = packet.a(((CraftWorld) world).getHandle());
        return a == null ? null : a.getBukkitEntity();
    }
}
