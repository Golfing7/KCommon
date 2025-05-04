package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.menu.action.ClickAction;
import com.golfing8.kcommon.menu.action.CloseRunnable;
import com.golfing8.kcommon.menu.marker.MenuClickHolder;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.util.ItemUtil;
import com.golfing8.kcommon.util.MS;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class MenuAbstract implements Menu {

    private Map<Integer, List<ClickAction>> actionMap;

    /**
     * The gui items to apply when the menu is 'updated'
     */
    private List<SimpleGUIItem> guiItems;
    @Getter
    private List<Placeholder> placeholders;
    @Getter
    private List<MultiLinePlaceholder> multiLinePlaceholders;
    /** The menu's ID */
    @Getter
    private UUID menuID = UUID.randomUUID();

    private Inventory backingInventory;
    private String title;
    private boolean canExpire;
    private int size;
    private boolean clickable;
    private boolean valid;
    @Getter @Setter
    private Runnable tickRunnable;

    private boolean recreate;

    private CloseRunnable onClose, postClose;

    private ClickAction bottomClickEvent, topClickEvent;
    private MenuShape menuShape;

    public MenuAbstract(String title, MenuShape shape, boolean clickable, boolean canExpire, Map<Integer, List<ClickAction>> actionMap,
                        List<Placeholder> placeholders, List<MultiLinePlaceholder> multiLinePlaceholders){
        this.menuShape = shape;
        if (shape.getType().isSizeMutable()) {
            this.backingInventory = NMS.getTheNMS().createInventory(new MenuClickHolder(clickable, this), shape.getSize(), MS.parseSingle(title, placeholders));
        } else {
            this.backingInventory = NMS.getTheNMS().createInventory(new MenuClickHolder(clickable, this), shape.getType().getType(), MS.parseSingle(title, placeholders));
        }
        this.guiItems = new ArrayList<>();
        this.canExpire = canExpire;
        this.size = shape.getSize();
        this.clickable = clickable;
        this.placeholders = placeholders;
        this.multiLinePlaceholders = multiLinePlaceholders;

        this.actionMap = actionMap;
        register();
    }

    @Override
    public void setMultiLinePlaceholders(List<MultiLinePlaceholder> multiLinePlaceholders) {
        this.multiLinePlaceholders = new ArrayList<>(multiLinePlaceholders);
    }

    @Override
    public void setPlaceholders(List<Placeholder> placeholders) {
        this.placeholders = new ArrayList<>(placeholders);
    }

    @Override
    public MenuShape getMenuShape() {
        return menuShape;
    }

    @Override
    public void onClose(CloseRunnable runnable) {
        this.onClose = runnable;
    }

    @Override
    public void onPostClose(CloseRunnable runnable) {
        this.postClose = runnable;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public boolean register() {
        // Are we already registered?
        if (this.valid)
            return false;

        this.valid = true;
        Bukkit.getServer().getPluginManager().registerEvents(this, KCommon.getInstance());
        MenuManager.getInstance().addMenu(this);
        return true;
    }

    @Override
    public void shutdown() {
        getViewers().forEach(HumanEntity::closeInventory);
        HandlerList.unregisterAll(this);
        valid = false;
    }

    @Override
    public Inventory getGUI() {
        return backingInventory;
    }

    @Override
    public void onTick() {
        if (this.tickRunnable != null)
            this.tickRunnable.run();
    }

    @Override
    public void open(Player player) {
        if (!this.valid) {
            this.register();
        }

        player.openInventory(this.backingInventory);
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
        ItemUtil.applyPlaceholders(set, placeholders);
        ItemUtil.applyMPlaceholders(set, multiLinePlaceholders);
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

            int slot = MenuUtils.getSlotFromCartCoords(getMenuShape().getType(), item.getSlot().getX(), item.getSlot().getY());
            this.setItemAt(slot, item.getItem().buildFromTemplate());
        }

        this.updateViewers();
    }

    @Override
    public List<SimpleGUIItem> getSpecialItems() {
        return new ArrayList<>(this.guiItems);
    }

    @Override
    public void setSpecialItems(List<SimpleGUIItem> specialItems) {
        this.guiItems = new ArrayList<>(specialItems);
    }

    @Override
    public void setTitle(String title) {
        this.title = MS.parseSingle(title, this.placeholders);
        recreate = true;
    }

    @Override
    public void setBottomClickAction(ClickAction action) {
        this.bottomClickEvent = action;
    }

    @Override
    public void setTopClickAction(ClickAction action) {
        this.topClickEvent = action;
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
        for (ItemStack itemStack : contents) {
            if (itemStack == null)
                continue;

            ItemUtil.applyPlaceholders(itemStack, placeholders);
            ItemUtil.applyMPlaceholders(itemStack, multiLinePlaceholders);
        }

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
            if(z instanceof Player) {
                Player player = (Player) z;
                if (player.getOpenInventory() == null)
                    return;

                InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
                if (!(holder instanceof MenuClickHolder))
                    return;

                if (((MenuClickHolder) holder).getMenu() != this)
                    return;

                toReturn.add(player);
            }
        });
        return toReturn;
    }

    @Override
    public void updateViewers() {
        if(recreate){
            List<Player> viewing = getViewers();

            viewing.forEach(Player::closeInventory);

            ItemStack[] contents = getContents();

            if (menuShape.getType().isSizeMutable()) {
                this.backingInventory = NMS.getTheNMS().createInventory(new MenuClickHolder(clickable, this), menuShape.getSize(), MS.parseSingle(title, placeholders));
            } else {
                this.backingInventory = NMS.getTheNMS().createInventory(new MenuClickHolder(clickable, this), menuShape.getType().getType(), MS.parseSingle(title, placeholders));
            }

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
        if (event.getWhoClicked().getOpenInventory().getTopInventory() == null)
            return;

        InventoryHolder holder = event.getWhoClicked().getOpenInventory().getTopInventory().getHolder();
        if (!(holder instanceof MenuClickHolder))
            return;

        MenuClickHolder clickHolder = (MenuClickHolder) holder;
        if (clickHolder.getMenu() != this)
            return;

        if(!clickable)event.setCancelled(true);

        Inventory clickedInventory = event.getClickedInventory();

        if(clickedInventory == null)return;

        if(clickedInventory != event.getWhoClicked().getOpenInventory().getTopInventory()){
            if(clickedInventory != event.getWhoClicked().getOpenInventory().getBottomInventory())
                return;

            //Run the bottom runnable.
            if(this.bottomClickEvent != null) {
                this.bottomClickEvent.attemptClick(event);
            }
            return;
        }

        if (this.topClickEvent != null) {
            this.topClickEvent.attemptClick(event);
        }
        if(actionMap.containsKey(event.getSlot())){
            actionMap.get(event.getSlot()).forEach(z -> z.attemptClick(event));
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        // Both checks are necessary as different Bukkit versions change how you can detect the inventory being closed.
        boolean wasInventory = event.getInventory() == getGUI() || getGUI().getViewers().contains(event.getPlayer());
        if(onClose != null && wasInventory){
            onClose.run(event);
        }

        if(postClose != null && wasInventory){
            Bukkit.getScheduler().runTask(KCommon.getInstance(), () -> postClose.run(event));
        }
    }
}
