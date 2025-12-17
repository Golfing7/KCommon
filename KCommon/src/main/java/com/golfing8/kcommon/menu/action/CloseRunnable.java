package com.golfing8.kcommon.menu.action;

import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * A consumer for players closing a menu
 */
public interface CloseRunnable {
    /**
     * Called when the menu is being closed
     *
     * @param event the event
     */
    void run(InventoryCloseEvent event);
}
