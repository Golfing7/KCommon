package com.golfing8.kcommon.nms.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PrepareResultEvent extends InventoryEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    @Getter
    @Setter
    private ItemStack result;

    public PrepareResultEvent(InventoryView transaction, @Nullable ItemStack result) {
        super(transaction);
        this.result = result;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
