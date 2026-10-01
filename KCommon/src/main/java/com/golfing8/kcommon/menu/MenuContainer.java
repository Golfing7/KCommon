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
        getMenu().open(player);
    }

    /**
     * Refreshes the opened menu w/ new items, click actions, placeholders, etc.
     */
    public void refresh() {
        if (menu == null)
            return;

        Menu newMenu = loadMenu();

        menu.setContents(newMenu.getContents());
        menu.setClickActions(newMenu.getClickActions());
        menu.setSpecialItems(newMenu.getSpecialItems());
        menu.setPlaceholders(newMenu.getPlaceholders());
        menu.setMultiLinePlaceholders(newMenu.getMultiLinePlaceholders());
        menu.refreshSpecialItems();
        menu.updateViewers();
    }

    /**
     * Refreshes the specific special slot.
     *
     * @param slot the slot
     */
    public void refreshSpecialSlot(String slot) {
        if (menu == null)
            return;

        Menu newMenu = getMenu();
        newMenu.refreshSpecialItem(slot);
    }
}
