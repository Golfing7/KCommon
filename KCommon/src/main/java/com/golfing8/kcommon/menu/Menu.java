package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.menu.action.ClickAction;
import com.golfing8.kcommon.menu.action.CloseRunnable;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.golfing8.kcommon.menu.MenuUtils.getSlotFromCartCoords;

/**
 * A menu is a basic interface for players to control and click buttons
 * using Minecraft inventories.
 */
public interface Menu extends Listener {
    /**
     * Gets a unique ID associated with this menu.
     *
     * @return the menu's unique ID.
     */
    UUID getMenuID();

    /**
     * Gets the shape of this menu.
     *
     * @return the actual shape.
     */
    MenuShape getMenuShape();

    /**
     * Gets the item occupying the slot specified
     *
     * @param slot the slot requested
     * @return the itemstack occupying the slot given
     */
    ItemStack getItemAt(int slot);

    /**
     * Gets the item occupying the inventory at X, Y coordinate
     * When the chest is a cartesian plane
     *
     * @param x the x coordinate of the slot
     * @param y the x coordinate of the slot
     * @return The item at the given coordinates
     */
    default ItemStack getItemAt(int x, int y) {
        return getItemAt(getSlotFromCartCoords(getMenuShape().getType(), x, y));
    }

    /**
     * Sets the item at a certain location in the inventory
     * and returns the item that was previously there
     *
     * @param slot The slot to set at
     * @param set  The item to set
     * @return The item that was in the slot
     */
    ItemStack setItemAt(int slot, ItemStack set);

    /**
     * Sets the item at a certain location in the inventory
     * and returns the item that was previously there
     *
     * @param x   The x coordinate of the slot
     * @param y   The y coordinate of the slot
     * @param set The item to set
     * @return The item that was in the slot
     */
    default ItemStack setItemAt(int x, int y, ItemStack set) {
        return setItemAt(getSlotFromCartCoords(getMenuShape().getType(), x, y), set);
    }

    /**
     * Gets the slots that are locked in this menu
     *
     * @return the locked slots
     */
    Set<Integer> getLockedSlots();

    /**
     * Sets the locked slots for this menu
     *
     * @param lockedSlots locked slots
     */
    void setLockedSlots(Set<Integer> lockedSlots);

    /**
     * Gets the tick this menu was created.
     *
     * @return the tick
     */
    long getCreatedTick();

    /**
     * Gets all placeholders registered to this menu
     *
     * @return the placeholders
     */
    List<Placeholder> getPlaceholders();

    /**
     * Sets the placeholders registered to this menu
     *
     * @param placeholders the placeholders
     */
    void setPlaceholders(List<Placeholder> placeholders);

    /**
     * Gets the multiline placeholders registered to this menu
     *
     * @return the multiline placeholders
     */
    List<MultiLinePlaceholder> getMultiLinePlaceholders();

    /**
     * Sets the multiline placeholders associated with this menu
     *
     * @param placeholders the multiline placeholders
     */
    void setMultiLinePlaceholders(List<MultiLinePlaceholder> placeholders);

    /**
     * Sets the top click action of the menu.
     *
     * @param action the click action.
     */
    void setTopClickAction(ClickAction action);

    /**
     * Sets the action for when players click their own inventory with this menu open
     *
     * @param action the action
     */
    void setBottomClickAction(ClickAction action);

    /**
     * Gets the title of the menu view
     *
     * @return the title
     */
    String getTitle();

    /**
     * Sets the title of the menu view
     *
     * @param title the title
     */
    void setTitle(String title);

    /**
     * Gets the size of the menu
     *
     * @return the size
     */
    int getSize();

    /**
     * Sets the size of the menu
     *
     * @param size the size
     */
    void setSize(int size);

    /**
     * Gets the contents of the menu
     *
     * @return the contents
     */
    ItemStack[] getContents();

    /**
     * Sets the contents of the menu
     *
     * @param contents the contents
     */
    void setContents(ItemStack[] contents);

    /**
     * Checks if players can click and take items out of the menu
     *
     * @return true if clickable
     */
    boolean canClick();

    /**
     * Sets if this menu is clickable/interactable
     *
     * @param clickable clickability
     */
    void setClickable(boolean clickable);

    /**
     * Add a special item to this menu
     *
     * @param key the key
     * @param item the item
     */
    void addSpecialItem(String key, SimpleGUIItem item);

    /**
     * Refresh every special item in this menu
     */
    void refreshSpecialItems();

    /**
     * Refresh a specific special item and all placeholders associated with it
     *
     * @param key the key
     */
    void refreshSpecialItem(String key);

    /**
     * Sets the registered special items for this menu
     *
     * @param specialItems the special items
     */
    void setSpecialItems(Map<String, SimpleGUIItem> specialItems);

    /**
     * Get all registered special items for this menu
     *
     * @return the special items
     */
    Map<String, SimpleGUIItem> getSpecialItems();

    /**
     * Checks if this menu is allowed to expire.
     * <p>
     * Menus that are allowed to expire will expire when there are no more viewers
     * </p>
     *
     * @return true if the menu can expire
     */
    boolean canExpire();

    /**
     * Sets if this menu can expire
     *
     * @param canExpire true if this can expire
     */
    void setCanExpire(boolean canExpire);

    /**
     * Checks if this menu is still valid to be opened by new players
     *
     * @return true if valid
     */
    boolean isValid();

    /**
     * Update all viewers of any changes
     */
    void updateViewers();

    /**
     * Gets the actions under the given slot
     *
     * @param slot the slot
     * @return the list of actions
     */
    List<ClickAction> getActionsAt(int slot);

    /**
     * Gets the actions at the given coordinate pair
     *
     * @param x the x
     * @param y the y
     * @return the list of actions
     */
    default List<ClickAction> getActionAt(int x, int y) {
        return getActionsAt(getSlotFromCartCoords(getMenuShape().getType(), x, y));
    }

    /**
     * Adds the click action under the given slot
     *
     * @param slot the slot
     * @param clickAction the action
     */
    void addClickAction(int slot, ClickAction clickAction);

    /**
     * Clears all click actions on the given slot
     *
     * @param slot the slot to clear
     */
    void clearClickActions(int slot);

    /**
     * Clear all click actions for the menu
     */
    void clearAllClickActions();

    /**
     * Gets all click actions for the menu
     *
     * @return the click actions
     */
    Map<Integer, List<ClickAction>> getClickActions();

    /**
     * Sets all click actions for this menu
     *
     * @param newActions the new actions
     */
    void setClickActions(Map<Integer, List<ClickAction>> newActions);

    /**
     * Sets the on close runnable
     *
     * @param runnable the runnable
     */
    void onClose(CloseRunnable runnable);

    /**
     * Set the post close runnable
     *
     * @param runnable the runnable
     */
    void onPostClose(CloseRunnable runnable);

    /**
     * Get the underlying inventory GUI
     *
     * @return the inventory
     */
    Inventory getGUI();

    /**
     * A tick method. Called once per second.
     */
    void onTick();

    /**
     * Opens the menu for the given player. If the menu has previously been GCed by the {@link MenuManager} it will be re-opened.
     *
     * @param player the player.
     */
    void open(Player player);

    /**
     * Registers this menu to the default {@link MenuManager}.
     *
     * @return true if the menu wasn't previously registered.
     */
    boolean register();

    /**
     * Shuts down the menu and all viewers are kicked out
     */
    void shutdown();

    /**
     * Gets all player viewers of the menu
     *
     * @return the viewers
     */
    List<Player> getViewers();
}
