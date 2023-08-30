package com.golfing8.kcommon.nms.v1_17.access;

import com.golfing8.kcommon.nms.access.NMSMagicEvents;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerFishEvent;

public class MagicEventsV1_17 implements NMSMagicEvents {
    @Override
    public Entity getFishingHook(PlayerFishEvent event) {
        return event.getHook();
    }
}
