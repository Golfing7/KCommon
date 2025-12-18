package com.golfing8.kcommon.nms;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A wrapper of a player and item hooks for item capturing
 */
@Getter
public class ItemCapturePlayer {
    private final Player player;
    private final List<Consumer<ItemStack>> inventoryItemHooks;

    public ItemCapturePlayer(Player player) {
        this.player = player;
        this.inventoryItemHooks = new ArrayList<>();
    }

    /**
     * Add an item hook
     *
     * @param hook the hook
     */
    public void add(Consumer<ItemStack> hook) {
        this.inventoryItemHooks.add(hook);
    }
}
