package com.golfing8.kcommon.menu.action;

import com.golfing8.kcommon.util.MS;
import org.bukkit.event.inventory.InventoryClickEvent;

public interface ClickRunnable {
    void click(InventoryClickEvent event);

    default void clickCooldown(InventoryClickEvent event) {
        event.getWhoClicked().sendMessage(MS.parseSingle("&cYou can't click this yet!"));
    }
}
