package com.golfing8.kcommon.nms.unknown.access;

import com.golfing8.kcommon.nms.access.NMSMagicEvents;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * API agnostic event access
 */
public class MagicEvents implements NMSMagicEvents {
    @Override
    public Entity getFishingHook(PlayerFishEvent event) {
        return event.getHook();
    }
}
