package com.golfing8.kcommon.nms.unknown.access;

import com.destroystokyo.paper.event.inventory.PrepareResultEvent;
import com.golfing8.kcommon.nms.access.NMSMagicInventories;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryView;

public class MagicInventories implements NMSMagicInventories, Listener {

    @EventHandler
    public void onPrepareResult(PrepareResultEvent event) {
        com.golfing8.kcommon.nms.event.PrepareResultEvent kEvent = new com.golfing8.kcommon.nms.event.PrepareResultEvent(event.getView(), event.getResult());
        kEvent.callEvent();

        event.setResult(kEvent.getResult());
    }
}
