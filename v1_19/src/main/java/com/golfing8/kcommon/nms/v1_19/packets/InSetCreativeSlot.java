package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSInSetCreativeSlot;
import lombok.AllArgsConstructor;
import net.minecraft.network.protocol.game.PacketPlayInSetCreativeSlot;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class InSetCreativeSlot implements NMSInSetCreativeSlot {
    private final PacketPlayInSetCreativeSlot packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public ItemStack getItemStack() {
        return CraftItemStack.asBukkitCopy(packet.c());
    }

    @Override
    public int getSlot() {
        return packet.b();
    }
}
