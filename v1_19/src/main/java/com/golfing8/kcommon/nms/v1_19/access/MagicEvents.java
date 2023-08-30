package com.golfing8.kcommon.nms.v1_19.access;

import com.golfing8.kcommon.nms.access.NMSMagicEvents;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerFishEvent;

public class MagicEvents implements NMSMagicEvents {
    @Override
    public Entity getFishingHook(PlayerFishEvent event) {
        return event.getHook();
    }
}
