package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.menu.action.ClickAction;
import com.golfing8.kcommon.menu.action.CloseRunnable;
import com.golfing8.kcommon.menu.marker.NoClickHolder;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.util.MS;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class MenuAbstract implements Menu {

    private Map<Integer, List<ClickAction>> actionMap;

    /**
     * The gui items to apply when the menu is 'updated'
     */
    private List<SimpleGUIItem> guiItems;

    private Inventory backingInventory;
    private String title;
    private boolean canExpire;
    private int size;
    private boolean clickable;
    private boolean valid = true;

    private boolean recreate;

    private CloseRunnable onClose;

    private ClickAction bottomClickEvent;

    public MenuAbstract(String title, int size, boolean clickable, boolean canExpire, Map<Integer, List<ClickAction>> actionMap){
        this.backingInventory = Bukkit.createInventory(clickable ? new NoClickHolder() : null, size, MS.parseSingle(title));
        this.guiItems = new ArrayList<>();
        this.canExpire = canExpire;
        this.size = size;
        this.clickable = clickable;

        this.actionMap = actionMap;

        Bukkit.getServer().getPluginManager().registerEvents(this, KCommon.getInstance());

        MenuManager.getInstance().addMenu(this);
    }

    @Override
    public void onClose(CloseRunnable runnable) {
        this.onClose = runnable;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void shutdown() {
        Menu.super.shutdown();
        valid = false;
    }

    @Override
    public Inventory getGUI() {
        return backingInventory;
    }

    @Override
    public List<ClickAction> getActionsAt(int slot) {
        return actionMap.get(slot);
    }

    @Override
    public void addClickAction(int slot, ClickAction clickAction) {
        if(actionMap.containsKey(slot)){
            actionMap.get(slot).add(clickAction);
        }else{
            actionMap.put(slot, Lists.newArrayList(clickAction));
        }
    }

    @Override
    public void clearClickActions(int slot) {
        actionMap.remove(slot);
    }

    @Override
    public void clearAllClickActions() {
        actionMap.clear();
    }

    @Override
    public Map<Integer, List<ClickAction>> getClickActions() {
        return Collections.unmodifiableMap(this.actionMap);
    }

    @Override
    public void setClickActions(Map<Integer, List<ClickAction>> newActions) {
        actionMap.clear();
        actionMap.putAll(newActions);
    }

    @Override
    public ItemStack getItemAt(int slot) {
        return backingInventory.getItem(slot);
    }

    @Override
    public ItemStack setItemAt(int slot, ItemStack set) {
        ItemStack there = getItemAt(slot);
        backingInventory.setItem(slot, set);
        return there;
    }

    /**
     * Adds a special gui item to this menu.
     *
     * @param item the item.
     */
    public void addSpecialItem(SimpleGUIItem item) {
        this.guiItems.add(item);
    }

    @Override
    public void refreshSpecialItems() {
        for(SimpleGUIItem item : this.guiItems) {
            ItemStackBuilder builder = item.getItem();

            //Add both placeholders.
            if(item.getSpecialMPlaceholders() != null)
                builder.multiLinePlaceholders(
                        item.getSpecialMPlaceholders().get().toArray(new MultiLinePlaceholder[0]));
            if(item.getSpecialPlaceholders() != null)
                builder.placeholders(item.getSpecialPlaceholders().get().toArray(new Placeholder[0]));

            int slot = MenuUtils.getSlotFromCartCoords(item.getSlot().getX(), item.getSlot().getY());
            this.setItemAt(slot, item.getItem().buildFromTemplate());
        }

        this.updateViewers();
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        recreate = true;
    }

    @Override
    public void setBottomClickAction(ClickAction action) {
        this.bottomClickEvent = action;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
        recreate = true;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public ItemStack[] getContents() {
        return backingInventory.getContents();
    }

    @Override
    public void setContents(ItemStack[] contents) {
        if(contents.length != backingInventory.getSize()){
            ItemStack[] padded = new ItemStack[backingInventory.getSize()];

            System.arraycopy(contents, 0, padded, 0, padded.length);

            backingInventory.setContents(padded);
        }else{
            backingInventory.setContents(contents);
        }
    }

    @Override
    public boolean canClick() {
        return clickable;
    }

    @Override
    public void setClickable(boolean clickable) {
        this.clickable = clickable;
    }

    @Override
    public boolean canExpire() {
        return canExpire;
    }

    @Override
    public void setCanExpire(boolean canExpire) {
        this.canExpire = canExpire;
    }

    @Override
    public List<Player> getViewers() {
        List<Player> toReturn = new ArrayList<>();
        this.backingInventory.getViewers().forEach(z -> {
            if(z instanceof Player)toReturn.add((Player) z);
        });
        return toReturn;
    }

    @Override
    public void updateViewers() {
        if(recreate){
            List<Player> viewing = getViewers();

            viewing.forEach(Player::closeInventory);

            ItemStack[] contents = getContents();

            this.backingInventory = Bukkit.createInventory(clickable ? new NoClickHolder() : null, size, MS.parseSingle(title));

            this.backingInventory.setContents(contents);

            viewing.forEach(z -> z.openInventory(this.backingInventory));
        }else{
            getViewers().forEach(Player::updateInventory);
        }
    }

    @Override
    public Menu clone() {
        return null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event){
        if(!backingInventory.getViewers().contains(event.getWhoClicked()))return;

        if(!clickable)event.setCancelled(true);

        Inventory clickedInventory = event.getClickedInventory();

        if(clickedInventory == null)return;

        if(clickedInventory != event.getWhoClicked().getOpenInventory().getTopInventory()){
            if(clickedInventory != event.getWhoClicked().getOpenInventory().getBottomInventory())
                return;

            //Run the bottom runnable.
            if(this.bottomClickEvent != null) {
                this.bottomClickEvent.attemptClick(event);
                return;
            }
        }

        if(actionMap.containsKey(event.getSlot())){
            actionMap.get(event.getSlot()).forEach(z -> z.attemptClick(event));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        if(onClose != null && this.backingInventory == event.getInventory()){
            onClose.run(event);
        }
    }
}
