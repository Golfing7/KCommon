package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.menu.action.ClickAction;
import com.golfing8.kcommon.menu.movement.MorphingItem;
import com.golfing8.kcommon.menu.movement.MovingItem;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MenuDynamic extends MenuAbstract {
    private final List<MorphingItem> morphingItems;
    private final List<MovingItem> movingItems;

    public MenuDynamic(String title, MenuShape shape, boolean clickable, boolean canExpire, Map<Integer, List<ClickAction>> actionMap,
                       List<Placeholder> placeholders, List<MultiLinePlaceholder> multiLinePlaceholders) {
        super(title, shape, clickable, canExpire, actionMap, placeholders, multiLinePlaceholders);

        this.morphingItems = new ArrayList<>();
        this.movingItems = new ArrayList<>();
    }

    public void tickDynamics() {
        for (MorphingItem morphingItem : morphingItems) {
            if (morphingItem.morphCheck()) {
                morphingItem.morph();
            }
        }

        for (MovingItem movingItem : movingItems) {
            if (movingItem.canMove()) {
                movingItem.move();
            }
        }

        updateViewers();
    }

    public void addMorphingItem(MorphingItem morphingItem) {
        morphingItems.add(morphingItem);
    }

    public void addMovingItem(MovingItem movingItem) {
        movingItems.add(movingItem);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        super.onClick(event);

        if (!getGUI().getViewers().contains(event.getWhoClicked())) {
            return;
        }

        if (!canClick()) {
            event.setCancelled(true);
        }

        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory == null) {
            return;
        }

        if (clickedInventory != event.getWhoClicked().getOpenInventory().getTopInventory()) {
            return;
        }

        for (MovingItem movingItem : movingItems) {
            if (movingItem.getCurrentSlot() == event.getSlot()) {
                movingItem.getClickActions().forEach(z -> z.attemptClick(event));
            }
        }
    }
}
