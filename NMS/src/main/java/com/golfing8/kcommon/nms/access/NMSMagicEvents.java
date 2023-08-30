package com.golfing8.kcommon.nms.access;

import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerFishEvent;

public interface NMSMagicEvents {
    Entity getFishingHook(PlayerFishEvent event);
}
