package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Packet wrapper for using items
 */
public interface NMSInUseItem extends NMSPacket {
    /**
     * The position the item was used
     *
     * @return the position
     */
    Position getPosition();

    /**
     * The face the item was used on
     *
     * @return the face
     */
    Direction getFace();

    /**
     * The item that was used
     *
     * @param player the player
     * @return the item
     */
    ItemStack getItemStack(Player player);
}
