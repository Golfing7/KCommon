package com.golfing8.kcommon.nms.unknown.access;

import com.golfing8.kcommon.nms.access.NMSMagicBlocks;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;

public class MagicBlocks implements NMSMagicBlocks {
    @Override
    public void setSkullOwner(Skull skull, OfflinePlayer offlinePlayer) {
        skull.setOwningPlayer(offlinePlayer);
    }
}
