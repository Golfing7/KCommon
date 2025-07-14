package com.golfing8.kcommon.event;

import com.golfing8.kcommon.struct.item.FancyItemDrop;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

/**
 * An event called when a player is *trying* to pick up an item out of a fancy item drop.
 */
@Getter
public class FancyItemPrePickupEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final FancyItemDrop itemDrop;
    /**
     * The item that the player picked up out of the fancy item drop
     */
    private final ItemStack itemPickedUp;

    /**
     * If set to true, this will remove the item from the fancy drop
     */
    @Setter
    private boolean itemConsumed;
    @Setter
    private boolean cancelled;

    public FancyItemPrePickupEvent(Player who, FancyItemDrop itemDrop, ItemStack itemPickedUp) {
        super(who);

        this.itemDrop = itemDrop;
        this.itemPickedUp = itemPickedUp;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
