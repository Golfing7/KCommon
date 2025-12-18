package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.struct.Direction;
import com.golfing8.kcommon.nms.struct.Position;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Packet wrapper for block placing
 */
public interface NMSInBlockPlace extends NMSPacket {
    /**
     * Gets the item being placed in the packet
     *
     * @param player the player
     * @return the item
     */
    ItemStack getItemStack(Player player);

    /**
     * Gets the position of the block being placed
     *
     * @return the position
     */
    Position getPosition();

    /**
     * Gets the face of the block
     *
     * @return the face
     */
    Direction getFace();
}
