package com.golfing8.kcommon.menu.action;

import com.golfing8.kcommon.util.MS;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * An abstract click task to be called when a player clicks something
 */
public interface ClickRunnable {
    /**
     * The consumer to run when a player clicks
     *
     * @param event the event
     */
    void click(InventoryClickEvent event);

    /**
     * Called when a click was on cooldown
     *
     * @param event the event
     */
    default void clickCooldown(InventoryClickEvent event) {
        event.getWhoClicked().sendMessage(MS.parseSingle("&cYou can't click this yet!"));
    }
}
