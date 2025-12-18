package com.golfing8.kcommon.nms.unknown.access;

import com.golfing8.kcommon.nms.access.NMSMagicBlocks;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;

/**
 * API agnostic block access
 */
public class MagicBlocks implements NMSMagicBlocks {
    @Override
    public void setSkullOwner(Skull skull, OfflinePlayer offlinePlayer) {
        skull.setOwningPlayer(offlinePlayer);
    }

    @Override
    public boolean isPassable(Location location) {
        return location.getBlock().isPassable();
    }
}
