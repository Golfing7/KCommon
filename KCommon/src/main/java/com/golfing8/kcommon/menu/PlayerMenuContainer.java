package com.golfing8.kcommon.menu;

import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Represents a menu container that has a direct link to a player.
 */
public abstract class PlayerMenuContainer extends MenuContainer{
    @Getter
    private final Player player;
    public PlayerMenuContainer(Player player) {
        this.player = player;
    }

    /**
     * Opens the menu for the linked player.
     */
    public void open() {
        open(player);
    }

    /**
     * Opens the menu for the player.
     * <p>
     * The provided player may or may not be the player this menu has a link to. In that case this operation may be unsupported.
     * </p>
     *
     * @param player the player.
     */
    public void open(Player player) {
        player.openInventory(getMenu().getGUI());
    }
}
