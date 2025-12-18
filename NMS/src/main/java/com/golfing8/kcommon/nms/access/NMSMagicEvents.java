package com.golfing8.kcommon.nms.access;

import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerFishEvent;

/**
 * NMS access for bukkit events
 */
public interface NMSMagicEvents {
    /**
     * Get the fishing hook entity
     *
     * @param event the event to get from
     * @return the fishing hook
     */
    Entity getFishingHook(PlayerFishEvent event);
}
