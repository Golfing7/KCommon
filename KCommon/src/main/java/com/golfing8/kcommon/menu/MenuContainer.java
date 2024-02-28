package com.golfing8.kcommon.menu;

import org.bukkit.entity.Player;

/**
 * Represents a class that contains a menu.
 */
public abstract class MenuContainer {
    protected Menu menu;

    /**
     * Called when the menu needs to be loaded. May be lazily loaded.
     *
     * @return the loaded menu.
     */
    protected abstract Menu loadMenu();

    /**
     * Gets, or loads, the menu.
     *
     * @return the menu.
     */
    public Menu getMenu() {
        if (this.menu == null)
            this.menu = loadMenu();

        return menu;
    }

    /**
     * Opens the menu for the player.
     *
     * @param player the player.
     */
    public void open(Player player) {
        player.openInventory(getMenu().getGUI());
    }
}
