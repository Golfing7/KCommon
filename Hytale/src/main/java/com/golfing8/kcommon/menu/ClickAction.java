package com.golfing8.kcommon.menu;

/**
 * Runs when the player activates an element a menu bound.
 */
@FunctionalInterface
public interface ClickAction {
    /**
     * Called on the player's world thread when the element is activated.
     *
     * @param event the event.
     */
    void onClick(MenuClickEvent event);
}
