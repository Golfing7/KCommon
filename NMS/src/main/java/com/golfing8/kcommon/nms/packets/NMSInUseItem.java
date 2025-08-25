package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface NMSInUseItem extends NMSPacket {
    Position getPosition();

    Direction getFace();

    ItemStack getItemStack(Player player);
}
