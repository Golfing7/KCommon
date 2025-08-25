package com.golfing8.kcommon.nms;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
public class ItemCapturePlayer {
    private final Player player;
    private final List<Consumer<ItemStack>> inventoryItemHooks;

    public ItemCapturePlayer(Player player) {
        this.player = player;
        this.inventoryItemHooks = new ArrayList<>();
    }

    public void add(Consumer<ItemStack> hook) {
        this.inventoryItemHooks.add(hook);
    }
}
