package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.menu.action.ClickAction;
import com.golfing8.kcommon.menu.action.CloseRunnable;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

import static com.golfing8.kcommon.menu.MenuUtils.getSlotFromCartCoords;

public interface Menu extends Listener {
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
        return getItemAt(getSlotFromCartCoords(x, y));
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
        return setItemAt(getSlotFromCartCoords(x, y), set);
    }

    void setBottomClickAction(ClickAction action);

    String getTitle();

    void setTitle(String title);

    int getSize();

    void setSize(int size);

    ItemStack[] getContents();

    void setContents(ItemStack[] contents);

    boolean canClick();

    void setClickable(boolean clickable);

    void addSpecialItem(SimpleGUIItem item);

    void refreshSpecialItems();

    boolean canExpire();

    void setCanExpire(boolean canExpire);

    boolean isValid();

    void updateViewers();

    List<ClickAction> getActionsAt(int slot);

    default List<ClickAction> getActionAt(int x, int y) {
        return getActionsAt(getSlotFromCartCoords(x, y));
    }

    void addClickAction(int slot, ClickAction clickAction);

    void clearClickActions(int slot);
    void clearAllClickActions();
    Map<Integer, List<ClickAction>> getClickActions();
    void setClickActions(Map<Integer, List<ClickAction>> newActions);

    void onClose(CloseRunnable runnable);

    Inventory getGUI();

    default void shutdown() {
        getViewers().forEach(HumanEntity::closeInventory);
    }

    List<Player> getViewers();

    Menu clone();
}
