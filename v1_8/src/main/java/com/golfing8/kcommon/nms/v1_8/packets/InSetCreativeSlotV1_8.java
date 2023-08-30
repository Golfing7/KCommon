package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSInSetCreativeSlot;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.PacketPlayInSetCreativeSlot;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class InSetCreativeSlotV1_8 implements NMSInSetCreativeSlot {
    private final PacketPlayInSetCreativeSlot packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public ItemStack getItemStack() {
        return CraftItemStack.asBukkitCopy(packet.getItemStack());
    }

    @Override
    public int getSlot() {
        return packet.a();
    }
}
