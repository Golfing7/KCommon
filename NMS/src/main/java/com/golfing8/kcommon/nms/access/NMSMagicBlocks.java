package com.golfing8.kcommon.nms.access;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;

public interface NMSMagicBlocks {
    /**
     * Sets the owner of the given skull block.
     *
     * @param skull the skull
     * @param offlinePlayer the offline player.
     */
    void setSkullOwner(Skull skull, OfflinePlayer offlinePlayer);
}
