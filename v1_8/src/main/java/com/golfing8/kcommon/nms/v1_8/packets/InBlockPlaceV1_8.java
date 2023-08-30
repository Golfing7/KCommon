package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSInBlockPlace;
import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import net.minecraft.server.v1_8_R3.PacketPlayInBlockPlace;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InBlockPlaceV1_8 implements NMSInBlockPlace {
    private final PacketPlayInBlockPlace packet;

    public InBlockPlaceV1_8(PacketPlayInBlockPlace packet){
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return CraftItemStack.asBukkitCopy(packet.getItemStack());
    }

    @Override
    public Position getPosition() {
        return new Position(packet.a().getX(), packet.a().getY(), packet.a().getZ());
    }

    @Override
    public Direction getFace() {
        return Direction.fromOrdinal(packet.getFace());
    }
}
