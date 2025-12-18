package com.golfing8.kcommon.nms.access;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;

/**
 * NMS access for blocks
 */
public interface NMSMagicBlocks {
    /**
     * Sets the owner of the given skull block.
     *
     * @param skull         the skull
     * @param offlinePlayer the offline player.
     */
    void setSkullOwner(Skull skull, OfflinePlayer offlinePlayer);

    /**
     * Checks if the given block is passable
     *
     * @param location the location of the block
     * @return true if passable
     */
    boolean isPassable(Location location);
}
