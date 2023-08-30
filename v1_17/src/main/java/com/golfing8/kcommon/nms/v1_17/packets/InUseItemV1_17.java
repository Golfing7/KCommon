package com.golfing8.kcommon.nms.v1_17.packets;

import com.golfing8.kcommon.nms.packets.NMSInUseItem;
import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayInUseItem;
import net.minecraft.world.EnumHand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class InUseItemV1_17 implements NMSInUseItem {
    private final PacketPlayInUseItem packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Position getPosition() {
        return new Position(packet.c().getBlockPosition().getX(), packet.c().getBlockPosition().getY(), packet.c().getBlockPosition().getZ());
    }

    @Override
    public Direction getFace() {
        return Direction.fromOrdinal(packet.c().getDirection().ordinal());
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return packet.b() == EnumHand.a ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
    }
}
