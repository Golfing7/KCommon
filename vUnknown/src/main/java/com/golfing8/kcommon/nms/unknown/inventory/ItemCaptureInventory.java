package com.golfing8.kcommon.nms.unknown.inventory;

import com.golfing8.kcommon.nms.ItemCapturePlayer;
import net.minecraft.world.entity.player.Inventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemCaptureInventory extends CraftInventoryPlayer {
    private final ItemCapturePlayer player;

    public ItemCaptureInventory(Inventory inventory, ItemCapturePlayer player) {
        super(inventory);

        this.player = player;
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
        for (ItemStack item : items) {
            player.getInventoryItemHooks().forEach(consumer -> consumer.accept(item));
        }
        return new HashMap<>();
    }

    @Override
    public void setItem(int index, ItemStack item) {
        player.getInventoryItemHooks().forEach(consumer -> consumer.accept(item));
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        player.getInventoryItemHooks().forEach(consumer -> consumer.accept(stack));
    }
}
