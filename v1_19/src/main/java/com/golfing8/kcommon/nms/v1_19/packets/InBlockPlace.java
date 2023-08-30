package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSInBlockPlace;
import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import net.minecraft.network.protocol.game.PacketPlayInBlockPlace;
import net.minecraft.world.EnumHand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InBlockPlace implements NMSInBlockPlace {
    private final PacketPlayInBlockPlace packet;

    public InBlockPlace(PacketPlayInBlockPlace packet){
        this.packet = packet;
    }

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public ItemStack getItemStack(Player player) {
        return packet.b() == EnumHand.a ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
    }

    @Override
    public Position getPosition() {
        //This packet doesn't contain a position.
        return new Position(0, 0, 0);
    }

    @Override
    public Direction getFace() {
        //Nor a direction.
        return Direction.DOWN;
    }
}
