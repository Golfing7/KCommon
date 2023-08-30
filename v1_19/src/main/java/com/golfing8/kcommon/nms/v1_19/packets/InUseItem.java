package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSInUseItem;
import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayInUseItem;
import net.minecraft.world.EnumHand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class InUseItem implements NMSInUseItem {
    private final PacketPlayInUseItem packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public Position getPosition() {
        return new Position(packet.c().a().u(), packet.c().a().v(), packet.c().a().w());
    }

    @Override
    public Direction getFace() {
        return Direction.fromOrdinal(packet.c().b().d());
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return packet.b() == EnumHand.a ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
    }
}
